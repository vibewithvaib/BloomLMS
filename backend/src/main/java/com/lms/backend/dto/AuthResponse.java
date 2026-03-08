package com.lms.backend.dto;

import com.lms.backend.model.Role;

public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        String email,
        Role role
) {
}
