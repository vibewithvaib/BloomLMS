package com.lms.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmissionRequest(
        @NotBlank String content
) {
}
