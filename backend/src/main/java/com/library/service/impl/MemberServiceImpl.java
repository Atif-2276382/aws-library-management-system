package com.library.service.impl;

import com.library.dto.CreateMemberRequest;
import com.library.dto.UpdateMemberRequest;
import com.library.entity.Member;
import com.library.entity.User;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import com.library.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl
        implements MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    @Override
    public Member createMember(
            CreateMemberRequest request) {

        if(memberRepository
                .findByMembershipId(
                        request.getMembershipId())
                .isPresent()) {

            throw new RuntimeException(
                    "Membership ID already exists");
        }

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        Member member = new Member();

        member.setName(request.getName());
        member.setMembershipId(
                request.getMembershipId());
        member.setUser(user);

        return memberRepository.save(member);
    }

    @Override
    public List<Member> getAllMembers() {

        return memberRepository.findAll();
    }

    @Override
    public Member getMemberById(Long id) {

        return memberRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Member not found"));
    }

    @Override
    public Member updateMember(
            Long id,
            UpdateMemberRequest request) {

        Member member = memberRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Member not found"));

        member.setName(request.getName());
        member.setMembershipId(
                request.getMembershipId());

        return memberRepository.save(member);
    }

    @Override
    public void deleteMember(Long id) {

        Member member = memberRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Member not found"));

        memberRepository.delete(member);
    }
}