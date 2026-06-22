package com.library.controller;

import com.library.dto.LendingDtos;
import com.library.repository.MemberRepository;
import com.library.security.LibraryUserDetails;
import com.library.service.LendingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lendings")
public class LendingController {

    private static final Logger log = LoggerFactory.getLogger(LendingController.class);
    private final LendingService lendingService;
    private final MemberRepository memberRepository;

    public LendingController(LendingService lendingService, MemberRepository memberRepository) {
        this.lendingService = lendingService;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public List<LendingDtos.LendingResponse> getAll() {
        log.debug("Fetching all lending records");
        return lendingService.findAll();
    }

    @GetMapping("/my")
    public List<LendingDtos.LendingResponse> myHistory(@AuthenticationPrincipal LibraryUserDetails user) {
        log.debug("Fetching lending history for user={}", user.getUsername());
        Integer memberId = memberRepository.findByUserUserId(user.getUser().getUserId())
                .orElseThrow()
                .getMemberId();
        return lendingService.findByMemberId(memberId);
    }

    @GetMapping("/{id}")
    public LendingDtos.LendingResponse getById(@PathVariable Integer id) {
        return lendingService.findById(id);
    }

    @PostMapping
    public ResponseEntity<LendingDtos.LendingResponse> issue(@Valid @RequestBody LendingDtos.IssueRequest request) {
        log.info("Issuing book id={} to memberId={}", request.bookId(), request.memberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(lendingService.issue(request));
    }

    @PutMapping("/{id}")
    public LendingDtos.LendingResponse returnBook(@PathVariable Integer id) {
        log.info("Returning lending id={}", id);
        return lendingService.returnBook(id);
    }
}
