package com.lms.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record WikiPageRequest(
        @NotBlank String title,
        @NotBlank String content
) {
}
