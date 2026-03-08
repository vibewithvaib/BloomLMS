package com.lms.backend.dto;

import com.lms.backend.model.ReflectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReflectionRequest(
        @NotNull ReflectionType type,
        @NotBlank String title,
        @NotBlank String content
) {
}
