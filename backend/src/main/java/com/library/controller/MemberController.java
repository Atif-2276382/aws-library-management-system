package com.library.controller;

import com.library.dto.MemberDtos;
import com.library.service.MemberService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberDtos.MemberResponse> getAll(@RequestParam(required = false) String search) {
        log.debug("Fetching members list search={}", search);
        return memberService.findAll(search);
    }

    @GetMapping("/{id}")
    public MemberDtos.MemberResponse getById(@PathVariable Integer id) {
        log.debug("Fetching member by id={}", id);
        return memberService.findById(id);
    }

    @PostMapping
    public ResponseEntity<MemberDtos.MemberResponse> create(@Valid @RequestBody MemberDtos.MemberRequest request) {
        log.info("Creating member username={} membershipId={} email={}", request.username(), request.membershipId(), request.emailId());
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    @PutMapping("/{id}")
    public MemberDtos.MemberResponse update(@PathVariable Integer id, @Valid @RequestBody MemberDtos.MemberUpdateRequest request) {
        log.info("Updating member id={} membershipId={} email={}", id, request.membershipId(), request.emailId());
        return memberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("Deleting member id={}", id);
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
