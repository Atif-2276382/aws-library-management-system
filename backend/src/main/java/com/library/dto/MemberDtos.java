package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class MemberDtos {

    private MemberDtos() {
    }

    public record MemberRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 20) String membershipId,
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 6, max = 100) String password
    ) {
    }

    public record MemberUpdateRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 20) String membershipId
    ) {
    }

    public record MemberResponse(Integer memberId, String name, String membershipId, Integer userId, String username) {
    }
}
