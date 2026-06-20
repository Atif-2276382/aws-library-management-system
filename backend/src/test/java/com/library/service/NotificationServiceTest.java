package com.library.service;

import com.library.dto.NotificationDtos;
import com.library.entity.Member;
import com.library.entity.Notification;
import com.library.repository.LendingRepository;
import com.library.repository.MemberRepository;
import com.library.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private LendingRepository lendingRepository;
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void send_success() {
        Member member = new Member();
        member.setMemberId(1);
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(10L);
            return n;
        });

        NotificationDtos.NotificationResponse response = notificationService.send(
                new NotificationDtos.NotificationRequest(1, "Due soon", false));
        assertEquals(10L, response.id());
    }
}
