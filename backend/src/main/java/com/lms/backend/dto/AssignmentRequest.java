package com.lms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record AssignmentRequest(
        @NotBlank String title,
        String instructions,
        Instant dueDate
) {
}
