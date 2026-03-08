package com.lms.backend.dto;

import jakarta.validation.constraints.NotNull;

public record MemberAssignRequest(
        @NotNull Long studentId
) {
}
