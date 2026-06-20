package com.example.gptdemo.model;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String prompt) {
}
