package com.library.service;

import com.library.dto.MemberDtos;
import com.library.entity.Member;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.LendingRepository;
import com.library.repository.MemberRepository;
import com.library.repository.NotificationRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LendingRepository lendingRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private MemberService memberService;

    @Test
    void create_duplicateMembershipId_throws() {
        MemberDtos.MemberRequest request =
                new MemberDtos.MemberRequest("John", "M001", "john@example.com", "john", "password");
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(memberRepository.existsByMembershipId("M001")).thenReturn(true);
        assertThrows(BusinessException.class, () -> memberService.create(request));
    }

    @Test
    void delete_memberWithActiveLoans_throws() {
        Member member = new Member();
        member.setMemberId(1);
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
        when(lendingRepository.countByMemberMemberIdAndReturnDateIsNull(1)).thenReturn(1L);

        assertThrows(BusinessException.class, () -> memberService.delete(1));

        verify(notificationRepository, never()).deleteByMemberMemberId(1);
        verify(lendingRepository, never()).deleteByMemberMemberId(1);
        verify(memberRepository, never()).delete(member);
        verify(userRepository, never()).delete(any());
    }
}
