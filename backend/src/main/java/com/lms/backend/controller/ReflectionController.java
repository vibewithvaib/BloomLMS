package com.lms.backend.controller;

import com.lms.backend.dto.ReflectionRequest;
import com.lms.backend.model.Course;
import com.lms.backend.model.ReflectionEntry;
import com.lms.backend.model.User;
import com.lms.backend.repository.CourseRepository;
import com.lms.backend.repository.ReflectionEntryRepository;
import com.lms.backend.service.ActivityLogService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReflectionController {

    private final CourseRepository courseRepository;
    private final ReflectionEntryRepository reflectionEntryRepository;
    private final ActivityLogService activityLogService;

    @GetMapping("/courses/{courseId}/reflections")
    public List<Map<String, Object>> courseReflections(@PathVariable Long courseId) {
        Course course = getCourseOrThrow(courseId);
        return reflectionEntryRepository.findByCourse(course).stream().map(this::toReflectionMap).toList();
    }

    @PostMapping("/courses/{courseId}/reflections")
    public Map<String, Object> createReflection(@PathVariable Long courseId,
                                                @Valid @RequestBody ReflectionRequest request,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);

        ReflectionEntry entry = new ReflectionEntry();
        entry.setCourse(course);
        entry.setStudent(user);
        entry.setType(request.type());
        entry.setTitle(request.title());
        entry.setContent(request.content());
        ReflectionEntry saved = reflectionEntryRepository.save(entry);

        activityLogService.log(user, course, "REFLECTION_ADDED", Map.of(
                "reflectionId", saved.getId(),
                "type", saved.getType().name()
        ));

        return toReflectionMap(saved);
    }

    @GetMapping("/reflections/me")
    public List<Map<String, Object>> myReflections(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return reflectionEntryRepository.findByStudent(user).stream().map(this::toReflectionMap).toList();
    }

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private Map<String, Object> toReflectionMap(ReflectionEntry entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entry.getId());
        map.put("courseId", entry.getCourse().getId());
        map.put("type", entry.getType());
        map.put("title", entry.getTitle());
        map.put("content", entry.getContent());
        map.put("student", Map.of("id", entry.getStudent().getId(), "name", entry.getStudent().getFullName()));
        map.put("createdAt", entry.getCreatedAt());
        return map;
    }
}
