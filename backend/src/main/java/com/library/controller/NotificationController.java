package com.library.controller;

import com.library.dto.NotificationDtos;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.MemberRepository;
import com.library.security.LibraryUserDetails;
import com.library.service.NotificationService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Value("${scheduler.secret}")
    private String schedulerSecret;

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    public NotificationController(NotificationService notificationService, MemberRepository memberRepository) {
        this.notificationService = notificationService;
        this.memberRepository = memberRepository;
    }

    @PostMapping
    public ResponseEntity<NotificationDtos.NotificationResponse> send(
            @Valid @RequestBody NotificationDtos.NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.send(request));
    }

    @GetMapping("/member/{memberId}")
    public List<NotificationDtos.NotificationResponse> byMember(@PathVariable Integer memberId) {
        return notificationService.findByMember(memberId);
    }

    @GetMapping("/my")
    public List<NotificationDtos.NotificationResponse> myNotifications(
            @AuthenticationPrincipal LibraryUserDetails user) {
        Integer memberId = memberRepository.findByUserUserId(user.getUser().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"))
                .getMemberId();
        return notificationService.findByMember(memberId);
    }

    @PostMapping("/reminders")
public ResponseEntity<String> triggerReminders() {

    notificationService.sendDueAndOverdueReminders();

    return ResponseEntity.ok("Reminders processed successfully");
}

@PostMapping("/scheduler/overdue")
public ResponseEntity<String> triggerOverdueNotifications(  @RequestHeader("X-Scheduler-Key") String key) {

        if (!schedulerSecret.equals(key)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    notificationService.sendOverdueNotifications();

    return ResponseEntity.ok("Overdue notifications processed");
}
}
