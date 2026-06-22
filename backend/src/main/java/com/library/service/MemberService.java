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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
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
            log.warn("Member creation failed, username already exists={}", request.username());
            throw new BusinessException("Username already exists");
        }
        if (memberRepository.existsByMembershipId(request.membershipId())) {
            log.warn("Member creation failed, membership ID already exists={}", request.membershipId());
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
        Member saved = memberRepository.save(member);
        log.info("Created member id={} username={} membershipId={}", saved.getMemberId(), saved.getUser().getUsername(), saved.getMembershipId());
        return toResponse(saved);
    }

    @Transactional
    public MemberDtos.MemberResponse update(Integer id, MemberDtos.MemberUpdateRequest request) {
        Member member = getMember(id);
        if (!member.getMembershipId().equals(request.membershipId())
                && memberRepository.existsByMembershipId(request.membershipId())) {
            log.warn("Member update failed, membership ID already exists={} id={}", request.membershipId(), id);
            throw new BusinessException("Membership ID already exists");
        }
        member.setName(request.name());
        member.setMembershipId(request.membershipId());
        Member saved = memberRepository.save(member);
        log.info("Updated member id={} membershipId={}", id, saved.getMembershipId());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Member member = getMember(id);
        if (lendingRepository.countByMemberMemberIdAndReturnDateIsNull(id) > 0) {
            log.warn("Member delete blocked by active loans id={}", id);
            throw new BusinessException("Cannot delete member with active loans. Return all borrowed books first.");
        }
        notificationRepository.deleteByMemberMemberId(id);
        lendingRepository.deleteByMemberMemberId(id);
        User user = member.getUser();
        memberRepository.delete(member);
        memberRepository.flush();
        userRepository.delete(user);
        log.info("Deleted member id={} username={}", id, user.getUsername());
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
