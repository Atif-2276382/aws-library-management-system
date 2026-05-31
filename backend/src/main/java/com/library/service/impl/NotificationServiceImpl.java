package com.library.service.impl;

import com.library.dto.NotificationRequest;
import com.library.entity.Notification;
import com.library.repository.NotificationRepository;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository
            notificationRepository;

    @Override
    public Notification sendNotification(
            NotificationRequest request) {

        Notification notification =
                new Notification();

        notification.setRecipient(
                request.getRecipient());

        notification.setSubject(
                request.getSubject());

        notification.setMessage(
                request.getMessage());

        notification.setStatus(
                "SENT");

        notification.setSentAt(
                LocalDateTime.now());

        return notificationRepository
                .save(notification);
    }

    @Override
    public List<Notification>
    getAllNotifications() {

        return notificationRepository
                .findAll();
    }

    @Override
    public Notification getNotificationById(
            Long id) {

        return notificationRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Notification not found"));
    }
}