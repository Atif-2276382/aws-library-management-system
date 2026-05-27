package com.library.dto;


import lombok.Data;

@Data
public class UpdateMemberRequest {

    private String name;

    private String membershipId;
}