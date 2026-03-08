package com.lms.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record QuizQuestionRequest(
        @NotBlank String prompt,
        @NotBlank String optionA,
        @NotBlank String optionB,
        @NotBlank String optionC,
        @NotBlank String optionD,
        @NotBlank String correctOption
) {
}
