package com.lms.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record QuizRequest(
        @NotBlank String title,
        String instructions
) {
}
