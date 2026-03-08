package com.lms.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ForumThreadRequest(
        @NotBlank String title,
        String prompt
) {
}
