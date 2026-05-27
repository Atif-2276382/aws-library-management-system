package com.library.controller;

import com.library.dto.CreateMemberRequest;
import com.library.dto.UpdateMemberRequest;
import com.library.entity.Member;
import com.library.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public Member createMember(
            @RequestBody
            CreateMemberRequest request) {

        return memberService
                .createMember(request);
    }

    @GetMapping
    public List<Member> getAllMembers() {

        return memberService
                .getAllMembers();
    }

    @GetMapping("/{id}")
    public Member getMemberById(
            @PathVariable Long id) {

        return memberService
                .getMemberById(id);
    }

    @PutMapping("/{id}")
    public Member updateMember(
            @PathVariable Long id,
            @RequestBody
            UpdateMemberRequest request) {

        return memberService
                .updateMember(id,
                        request);
    }

    @DeleteMapping("/{id}")
    public String deleteMember(
            @PathVariable Long id) {

        memberService.deleteMember(id);

        return "Member deleted successfully";
    }
}