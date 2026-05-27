package com.library.dto;


import lombok.Data;

@Data
public class CreateMemberRequest {

    private String name;

    private String membershipId;

    private Long userId;
}