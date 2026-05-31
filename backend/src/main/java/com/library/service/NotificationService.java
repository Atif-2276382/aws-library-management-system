package com.library.service;

import com.library.dto.NotificationRequest;
import com.library.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification sendNotification(
            NotificationRequest request);

    List<Notification> getAllNotifications();

    Notification getNotificationById(
            Long id);

}