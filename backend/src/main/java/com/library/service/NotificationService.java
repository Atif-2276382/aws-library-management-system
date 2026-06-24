package com.library.service;

import com.library.dto.NotificationDtos;
import com.library.entity.Lending;
import com.library.entity.Member;
import com.library.entity.Notification;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.LendingRepository;
import com.library.repository.MemberRepository;
import com.library.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final LendingRepository lendingRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            MemberRepository memberRepository,
            LendingRepository lendingRepository) {
        this.notificationRepository = notificationRepository;
        this.memberRepository = memberRepository;
        this.lendingRepository = lendingRepository;
    }

    @Transactional
    public NotificationDtos.NotificationResponse send(NotificationDtos.NotificationRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        Notification notification = new Notification();
        notification.setMember(member);
        notification.setMessage(request.message());
        notification.setOverdue(request.overdue());
        notification.setSentAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);
        log.info("Notification sent to member {}: {} :", member.getMemberId(), request.message());
        return toResponse(notification);
    }

    public List<NotificationDtos.NotificationResponse> findByMember(Integer memberId) {
        return notificationRepository.findByMemberMemberIdOrderBySentAtDesc(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void sendDueAndOverdueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderThreshold = now.plusDays(2);
        List<Lending> lendings = lendingRepository.findDueOrOverdue(reminderThreshold);
        for (Lending lending : lendings) {
            boolean overdue = lending.getDueDate().isBefore(now);
            String status = overdue ? "OVERDUE" : "DUE SOON";
            String message = String.format(
                    "%s: Book '%s' is %s. Due date: %s.",
                    status,
                    lending.getBook().getTitle(),
                    overdue ? "overdue" : "due soon",
                    lending.getDueDate());
            send(new NotificationDtos.NotificationRequest(
                    lending.getMember().getMemberId(), message, overdue));
        }
    }

    @Transactional
public void sendOverdueNotifications() {

    List<Lending> lendings =
            lendingRepository.findOverdueBooks(LocalDateTime.now());

    for (Lending lending : lendings) {

        String message =
                String.format(
                        "Book '%s' is overdue. Due date was %s.",
                        lending.getBook().getTitle(),
                        lending.getDueDate());

        send(new NotificationDtos.NotificationRequest(
                lending.getMember().getMemberId(),
                message,
                true));

        // send email here
    }
}

    private NotificationDtos.NotificationResponse toResponse(Notification notification) {
        return new NotificationDtos.NotificationResponse(
                notification.getId(),
                notification.getMember().getMemberId(),
                notification.getMessage(),
                notification.isOverdue(),
                notification.getSentAt());
    }
}
