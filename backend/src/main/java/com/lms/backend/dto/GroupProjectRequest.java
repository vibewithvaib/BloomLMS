package com.lms.backend.dto;

import java.time.Instant;

public record GroupProjectRequest(
        String name,
        String description,
        Instant dueDate
) {
}
