package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class BookDtos {

    private BookDtos() {
    }

    public record BookRequest(
            @NotBlank @Size(max = 100) String title,
            @NotBlank @Size(max = 20) String isbn,
            @NotNull Integer authorId,
            @Size(max = 50) String genre,
            Boolean availability
    ) {
    }

    public record BookResponse(
            Integer bookId,
            String title,
            String isbn,
            Integer authorId,
            String authorName,
            String genre,
            boolean availability
    ) {
    }
}
