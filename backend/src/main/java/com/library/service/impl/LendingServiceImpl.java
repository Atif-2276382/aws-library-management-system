package com.library.service.impl;

import com.library.dto.IssueBookRequest;
import com.library.entity.Book;
import com.library.entity.Lending;
import com.library.entity.Member;
import com.library.repository.BookRepository;
import com.library.repository.LendingRepository;
import com.library.repository.MemberRepository;
import com.library.service.LendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LendingServiceImpl
        implements LendingService {

    private final LendingRepository lendingRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Override
    public Lending issueBook(
            IssueBookRequest request) {

        Book book = bookRepository
                .findById(request.getBookId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Book not found"));

        Member member = memberRepository
                .findById(request.getMemberId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Member not found"));

        if(!book.isAvailable()) {

            throw new RuntimeException(
                    "Book not available");
        }

        long borrowedBooks =
                lendingRepository
                        .countByMemberIdAndReturnDateIsNull(
                                member.getId());

        if(borrowedBooks >= 5) {

            throw new RuntimeException(
                    "Maximum borrowing limit reached");
        }

        Lending lending = new Lending();

        lending.setBook(book);

        lending.setMember(member);

        lending.setIssueDate(
                LocalDate.now());

        lending.setDueDate(
                LocalDate.now()
                        .plusDays(14));

        book.setAvailable(false);

        bookRepository.save(book);

        return lendingRepository
                .save(lending);
    }

    @Override
    public Lending returnBook(
            Long lendingId) {

        Lending lending =
                lendingRepository
                        .findById(lendingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Lending not found"));

        lending.setReturnDate(
                LocalDate.now());

        Book book = lending.getBook();

        book.setAvailable(true);

        bookRepository.save(book);

        return lendingRepository
                .save(lending);
    }

    @Override
    public List<Lending> getAllLendings() {

        return lendingRepository.findAll();
    }

    @Override
    public Lending getLendingById(
            Long id) {

        return lendingRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lending not found"));
    }
}