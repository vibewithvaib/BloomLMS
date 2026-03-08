package com.lms.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PeerReviewRequest(
        @NotBlank String feedback,
        @NotNull @Min(1) @Max(10) Integer score
) {
}
