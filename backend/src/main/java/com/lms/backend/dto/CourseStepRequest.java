package com.lms.backend.dto;

import com.lms.backend.model.ActivityType;
import com.lms.backend.model.BloomLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseStepRequest(
        @NotBlank String title,
        String description,
        @NotNull BloomLevel bloomLevel,
        @NotNull ActivityType activityType,
        @NotNull Integer stepOrder,
        String resourceUrl
) {
}
