package com.library.service;

import com.library.dto.LendingDtos;
import com.library.entity.Book;
import com.library.entity.Lending;
import com.library.entity.Member;
import com.library.exception.BusinessException;
import com.library.repository.BookRepository;
import com.library.repository.LendingRepository;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LendingServiceTest {

    @Mock
    private LendingRepository lendingRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private LendingService lendingService;

    @Test
    void issueBook_notAvailable_throws() {
        Book book = new Book();
        book.setBookId(1);
        book.setAvailability(false);
        Member member = new Member();
        member.setMemberId(2);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2)).thenReturn(Optional.of(member));

        assertThrows(BusinessException.class,
                () -> lendingService.issue(new LendingDtos.IssueRequest(1, 2)));
    }

    @Test
    void issueBook_maxLoans_throws() {
        Book book = new Book();
        book.setBookId(1);
        book.setAvailability(true);
        Member member = new Member();
        member.setMemberId(2);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(memberRepository.findById(2)).thenReturn(Optional.of(member));
        when(lendingRepository.countByMemberMemberIdAndReturnDateIsNull(2)).thenReturn(5L);

        assertThrows(BusinessException.class,
                () -> lendingService.issue(new LendingDtos.IssueRequest(1, 2)));
    }

    @Test
    void returnBook_alreadyReturned_throws() {
        Lending lending = new Lending();
        lending.setLendingId(1);
        lending.setReturnDate(java.time.LocalDateTime.now());

        when(lendingRepository.findById(1)).thenReturn(Optional.of(lending));

        assertThrows(BusinessException.class, () -> lendingService.returnBook(1));
        verify(bookRepository, never()).save(any());
    }
}
