package com.library.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class LendingDtos {

    private LendingDtos() {
    }

    public record IssueRequest(@NotNull Integer bookId, @NotNull Integer memberId) {
    }

    public record LendingResponse(
            Integer lendingId,
            Integer bookId,
            String bookTitle,
            Integer memberId,
            String memberName,
            LocalDateTime issueDate,
            LocalDateTime dueDate,
            LocalDateTime returnDate,
            boolean overdue
    ) {
    }
}
