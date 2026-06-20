package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AuthorDtos {

    private AuthorDtos() {
    }

    public record AuthorRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record AuthorResponse(Integer authorId, String name, List<String> linkedBooks) {
    }
}
