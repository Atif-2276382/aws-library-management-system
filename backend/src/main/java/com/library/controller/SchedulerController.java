package com.library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.service.NotificationService;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private final NotificationService notificationService;

    public SchedulerController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/overdue-reminders")
    public ResponseEntity<String> sendReminders() {

        notificationService.sendDueAndOverdueReminders();

        return ResponseEntity.ok("Reminders processed");
    }
}