package com.library.service;

import com.library.dto.MemberDtos;
import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.LendingRepository;
import com.library.repository.MemberRepository;
import com.library.repository.NotificationRepository;
import com.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final LendingRepository lendingRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(
            MemberRepository memberRepository,
            UserRepository userRepository,
            LendingRepository lendingRepository,
            NotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.lendingRepository = lendingRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<MemberDtos.MemberResponse> findAll(String search) {
        List<Member> members = search == null || search.isBlank()
                ? memberRepository.findAll()
                : memberRepository.findByNameContainingIgnoreCaseOrMembershipIdContainingIgnoreCase(search, search);
        return members.stream().map(this::toResponse).toList();
    }

    public MemberDtos.MemberResponse findById(Integer id) {
        return toResponse(getMember(id));
    }

    @Transactional
    public MemberDtos.MemberResponse create(MemberDtos.MemberRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already exists");
        }
        if (memberRepository.existsByMembershipId(request.membershipId())) {
            throw new BusinessException("Membership ID already exists");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.MEMBER);
        user = userRepository.save(user);

        Member member = new Member();
        member.setName(request.name());
        member.setMembershipId(request.membershipId());
        member.setUser(user);
        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public MemberDtos.MemberResponse update(Integer id, MemberDtos.MemberUpdateRequest request) {
        Member member = getMember(id);
        if (!member.getMembershipId().equals(request.membershipId())
                && memberRepository.existsByMembershipId(request.membershipId())) {
            throw new BusinessException("Membership ID already exists");
        }
        member.setName(request.name());
        member.setMembershipId(request.membershipId());
        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public void delete(Integer id) {
        Member member = getMember(id);
        if (lendingRepository.countByMemberMemberIdAndReturnDateIsNull(id) > 0) {
            throw new BusinessException("Cannot delete member with active loans. Return all borrowed books first.");
        }
        notificationRepository.deleteByMemberMemberId(id);
        lendingRepository.deleteByMemberMemberId(id);
        User user = member.getUser();
        memberRepository.delete(member);
        memberRepository.flush();
        userRepository.delete(user);
    }

    private Member getMember(Integer id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    private MemberDtos.MemberResponse toResponse(Member member) {
        return new MemberDtos.MemberResponse(
                member.getMemberId(),
                member.getName(),
                member.getMembershipId(),
                member.getUser().getUserId(),
                member.getUser().getUsername());
    }
}
