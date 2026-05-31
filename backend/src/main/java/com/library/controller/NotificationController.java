package com.library.controller;

import com.library.dto.NotificationRequest;
import com.library.entity.Notification;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService
            notificationService;

    @PostMapping("/send")
    public Notification sendNotification(
            @RequestBody
            NotificationRequest request) {

        return notificationService
                .sendNotification(request);
    }

    @GetMapping
    public List<Notification>
    getAllNotifications() {

        return notificationService
                .getAllNotifications();
    }

    @GetMapping("/{id}")
    public Notification getNotificationById(
            @PathVariable Long id) {

        return notificationService
                .getNotificationById(id);
    }
}