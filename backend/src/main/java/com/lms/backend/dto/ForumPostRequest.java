package com.lms.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ForumPostRequest(
        @NotBlank String content,
        Long parentPostId
) {
}
