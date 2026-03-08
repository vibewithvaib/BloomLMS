package com.lms.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.backend.model.ActivityLog;
import com.lms.backend.model.Course;
import com.lms.backend.model.User;
import com.lms.backend.repository.ActivityLogRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    public void log(User user, Course course, String actionType, Map<String, Object> metadata) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setCourse(course);
        log.setActionType(actionType);

        try {
            log.setMetadata(metadata == null ? null : objectMapper.writeValueAsString(metadata));
        } catch (JsonProcessingException ignored) {
            log.setMetadata(null);
        }

        activityLogRepository.save(log);
    }
}
