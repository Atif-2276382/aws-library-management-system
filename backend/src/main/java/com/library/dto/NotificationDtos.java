package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class NotificationDtos {

    private NotificationDtos() {
    }

    public record NotificationRequest(
            @NotNull Integer memberId,
            @NotBlank String message,
            boolean overdue
    ) {
    }

    public record NotificationResponse(Long id, Integer memberId, String message, boolean overdue, LocalDateTime sentAt) {
    }
}
