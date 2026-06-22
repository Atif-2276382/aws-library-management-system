package com.library.service;

import com.library.dto.LendingDtos;
import com.library.entity.Book;
import com.library.entity.Lending;
import com.library.entity.Member;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LendingRepository;
import com.library.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LendingService {

    private static final Logger log = LoggerFactory.getLogger(LendingService.class);
    public static final int MAX_ACTIVE_LOANS = 5;
    public static final int LOAN_PERIOD_DAYS = 14;

    private final LendingRepository lendingRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LendingService(
            LendingRepository lendingRepository,
            BookRepository bookRepository,
            MemberRepository memberRepository) {
        this.lendingRepository = lendingRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    public List<LendingDtos.LendingResponse> findAll() {
        return lendingRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<LendingDtos.LendingResponse> findByMemberId(Integer memberId) {
        return lendingRepository.findByMemberMemberId(memberId).stream().map(this::toResponse).toList();
    }

    public LendingDtos.LendingResponse findById(Integer id) {
        return toResponse(getLending(id));
    }

    @Transactional
    public LendingDtos.LendingResponse issue(LendingDtos.IssueRequest request) {
        log.debug("Issuing lending request for bookId={} memberId={}", request.bookId(), request.memberId());
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!book.isAvailability()) {
            log.warn("Lending failed, book not available bookId={}", request.bookId());
            throw new BusinessException("Book is not available for lending");
        }
        long activeLoans = lendingRepository.countByMemberMemberIdAndReturnDateIsNull(member.getMemberId());
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            log.warn("Lending failed, member reached active loan limit memberId={}", member.getMemberId());
            throw new BusinessException("Member has reached the maximum of 5 active loans");
        }

        LocalDateTime now = LocalDateTime.now();
        Lending lending = new Lending();
        lending.setBook(book);
        lending.setMember(member);
        lending.setIssueDate(now);
        lending.setDueDate(now.plusDays(LOAN_PERIOD_DAYS));

        book.setAvailability(false);
        bookRepository.save(book);

        Lending saved = lendingRepository.save(lending);
        log.info("Created lending id={} bookId={} memberId={}", saved.getLendingId(), request.bookId(), request.memberId());
        return toResponse(saved);
    }

    @Transactional
    public LendingDtos.LendingResponse returnBook(Integer lendingId) {
        log.debug("Returning lending id={}", lendingId);
        Lending lending = getLending(lendingId);
        if (lending.getReturnDate() != null) {
            log.warn("Return failed, already returned lending id={}", lendingId);
            throw new BusinessException("Book has already been returned");
        }
        lending.setReturnDate(LocalDateTime.now());
        Book book = lending.getBook();
        book.setAvailability(true);
        bookRepository.save(book);
        Lending saved = lendingRepository.save(lending);
        log.info("Returned lending id={} bookId={}", saved.getLendingId(), book.getBookId());
        return toResponse(saved);
    }

    private Lending getLending(Integer id) {
        return lendingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lending not found"));
    }

    private LendingDtos.LendingResponse toResponse(Lending lending) {
        boolean overdue = lending.isActive() && lending.getDueDate().isBefore(LocalDateTime.now());
        return new LendingDtos.LendingResponse(
                lending.getLendingId(),
                lending.getBook().getBookId(),
                lending.getBook().getTitle(),
                lending.getMember().getMemberId(),
                lending.getMember().getName(),
                lending.getIssueDate(),
                lending.getDueDate(),
                lending.getReturnDate(),
                overdue);
    }
}
