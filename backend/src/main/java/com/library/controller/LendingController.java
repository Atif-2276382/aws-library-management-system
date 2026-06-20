package com.library.controller;

import com.library.dto.LendingDtos;
import com.library.repository.MemberRepository;
import com.library.security.LibraryUserDetails;
import com.library.service.LendingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lendings")
public class LendingController {

    private final LendingService lendingService;
    private final MemberRepository memberRepository;

    public LendingController(LendingService lendingService, MemberRepository memberRepository) {
        this.lendingService = lendingService;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public List<LendingDtos.LendingResponse> getAll() {
        return lendingService.findAll();
    }

    @GetMapping("/my")
    public List<LendingDtos.LendingResponse> myHistory(@AuthenticationPrincipal LibraryUserDetails user) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(lendingService.issue(request));
    }

    @PutMapping("/{id}")
    public LendingDtos.LendingResponse returnBook(@PathVariable Integer id) {
        return lendingService.returnBook(id);
    }
}
