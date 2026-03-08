package com.lms.backend.controller;

import com.lms.backend.model.ActivityLog;
import com.lms.backend.model.BloomLevel;
import com.lms.backend.model.Course;
import com.lms.backend.model.CourseStep;
import com.lms.backend.model.Enrollment;
import com.lms.backend.model.QuizAttempt;
import com.lms.backend.model.User;
import com.lms.backend.repository.ActivityLogRepository;
import com.lms.backend.repository.CourseRepository;
import com.lms.backend.repository.CourseStepRepository;
import com.lms.backend.repository.EnrollmentRepository;
import com.lms.backend.repository.QuizAttemptRepository;
import com.lms.backend.repository.ReflectionEntryRepository;
import com.lms.backend.repository.SubmissionRepository;
import com.lms.backend.service.AccessControlService;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final CourseRepository courseRepository;
    private final CourseStepRepository courseStepRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final SubmissionRepository submissionRepository;
    private final ReflectionEntryRepository reflectionEntryRepository;
    private final AccessControlService accessControlService;

    @GetMapping("/me")
    public Map<String, Object> myAnalytics(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<Enrollment> enrollments = enrollmentRepository.findByStudent(user);
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudent(user);
        List<ActivityLog> logs = activityLogRepository.findByUserOrderByCreatedAtDesc(user);

        double avgQuizScore = attempts.stream()
                .mapToDouble(a -> a.getTotal() == 0 ? 0 : ((double) a.getScore() / a.getTotal()) * 100)
                .average()
                .orElse(0.0);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        map.put("enrollments", enrollments.size());
        map.put("averageProgress", enrollments.stream().mapToInt(Enrollment::getProgressPercent).average().orElse(0));
        map.put("averageQuizScore", Math.round(avgQuizScore * 100.0) / 100.0);
        map.put("reflectionCount", reflectionEntryRepository.findByStudent(user).size());
        map.put("submissionCount", submissionRepository.findByStudent(user).size());
        map.put("recentActivity", logs.stream().limit(10).map(this::toLogMap).toList());
        return map;
    }

    @GetMapping("/courses/{courseId}/overview")
    public Map<String, Object> courseOverview(@PathVariable Long courseId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);

        if (!accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course owner or admin can access course analytics");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
        List<ActivityLog> logs = activityLogRepository.findByCourseOrderByCreatedAtDesc(course);
        List<CourseStep> steps = courseStepRepository.findByCourseOrderByStepOrderAsc(course);

        Map<BloomLevel, Integer> bloomCoverage = new EnumMap<>(BloomLevel.class);
        for (BloomLevel level : BloomLevel.values()) {
            bloomCoverage.put(level, 0);
        }
        for (CourseStep step : steps) {
            bloomCoverage.put(step.getBloomLevel(), bloomCoverage.get(step.getBloomLevel()) + 1);
        }

        Map<String, Long> activityBreakdown = new HashMap<>();
        for (ActivityLog log : logs) {
            activityBreakdown.put(log.getActionType(), activityBreakdown.getOrDefault(log.getActionType(), 0L) + 1);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("courseId", course.getId());
        map.put("courseTitle", course.getTitle());
        map.put("enrollmentCount", enrollments.size());
        map.put("avgProgress", enrollments.stream().mapToInt(Enrollment::getProgressPercent).average().orElse(0));
        map.put("activeParticipants", logs.stream().map(log -> log.getUser().getId()).distinct().count());
        map.put("activityBreakdown", activityBreakdown);
        map.put("bloomCoverage", bloomCoverage);
        map.put("bloomBalanced", bloomCoverage.values().stream().noneMatch(v -> v == 0));
        map.put("recentActivity", logs.stream().limit(25).map(this::toLogMap).toList());
        return map;
    }

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private Map<String, Object> toLogMap(ActivityLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("userId", log.getUser().getId());
        map.put("userName", log.getUser().getFullName());
        map.put("actionType", log.getActionType());
        map.put("metadata", log.getMetadata());
        map.put("createdAt", log.getCreatedAt());
        return map;
    }
}
