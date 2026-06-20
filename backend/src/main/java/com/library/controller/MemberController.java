package com.library.controller;

import com.library.dto.MemberDtos;
import com.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberDtos.MemberResponse> getAll(@RequestParam(required = false) String search) {
        return memberService.findAll(search);
    }

    @GetMapping("/{id}")
    public MemberDtos.MemberResponse getById(@PathVariable Integer id) {
        return memberService.findById(id);
    }

    @PostMapping
    public ResponseEntity<MemberDtos.MemberResponse> create(@Valid @RequestBody MemberDtos.MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    @PutMapping("/{id}")
    public MemberDtos.MemberResponse update(@PathVariable Integer id, @Valid @RequestBody MemberDtos.MemberUpdateRequest request) {
        return memberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
