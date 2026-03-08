package com.lms.backend.dto;

import java.util.Map;

public record QuizAttemptRequest(
        Map<Long, String> answers
) {
}
