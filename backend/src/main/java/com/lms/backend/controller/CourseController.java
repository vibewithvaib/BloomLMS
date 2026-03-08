package com.lms.backend.controller;

import com.lms.backend.dto.CourseRequest;
import com.lms.backend.dto.CourseStepRequest;
import com.lms.backend.model.BloomLevel;
import com.lms.backend.model.Course;
import com.lms.backend.model.CourseStep;
import com.lms.backend.model.Enrollment;
import com.lms.backend.model.EnrollmentStatus;
import com.lms.backend.model.Role;
import com.lms.backend.model.User;
import com.lms.backend.repository.CourseRepository;
import com.lms.backend.repository.CourseStepRepository;
import com.lms.backend.repository.EnrollmentRepository;
import com.lms.backend.service.AccessControlService;
import com.lms.backend.service.ActivityLogService;
import jakarta.validation.Valid;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;
    private final CourseStepRepository courseStepRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AccessControlService accessControlService;
    private final ActivityLogService activityLogService;

    @GetMapping
    public List<Map<String, Object>> listCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<Course> courses;
        if (accessControlService.isAdmin(user)) {
            courses = courseRepository.findAll();
        } else if (accessControlService.isInstructor(user)) {
            courses = courseRepository.findByInstructor(user);
        } else {
            courses = courseRepository.findByPublishedTrue();
        }

        return courses.stream().map(c -> toCourseMap(c, user)).toList();
    }

    @PostMapping
    public Map<String, Object> createCourse(@Valid @RequestBody CourseRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!accessControlService.isInstructor(user) && !accessControlService.isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only instructors/admins can create courses");
        }

        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setInstructor(user);
        course.setPublished(false);

        Course saved = courseRepository.save(course);
        activityLogService.log(user, saved, "COURSE_CREATED", Map.of("courseId", saved.getId(), "title", saved.getTitle()));
        return toCourseMap(saved, user);
    }

    @GetMapping("/{courseId}")
    public Map<String, Object> getCourse(@PathVariable Long courseId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);

        if (!course.isPublished() && !accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course is not published");
        }

        return toCourseMap(course, user);
    }

    @PatchMapping("/{courseId}/publish")
    public Map<String, Object> publishCourse(@PathVariable Long courseId,
                                             @RequestParam(defaultValue = "true") boolean published,
                                             Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);
        if (!accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course owner or admin can publish");
        }

        course.setPublished(published);
        Course saved = courseRepository.save(course);
        activityLogService.log(user, course, "COURSE_PUBLISH_STATUS_CHANGED", Map.of("published", published));
        return toCourseMap(saved, user);
    }

    @GetMapping("/{courseId}/steps")
    public List<Map<String, Object>> listSteps(@PathVariable Long courseId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);

        if (!course.isPublished() && !accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course is not published");
        }

        return courseStepRepository.findByCourseOrderByStepOrderAsc(course)
                .stream()
                .map(this::toStepMap)
                .toList();
    }

    @PostMapping("/{courseId}/steps")
    public Map<String, Object> addStep(@PathVariable Long courseId,
                                        @Valid @RequestBody CourseStepRequest request,
                                        Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);
        if (!accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course owner or admin can add steps");
        }

        CourseStep step = new CourseStep();
        step.setCourse(course);
        step.setTitle(request.title());
        step.setDescription(request.description());
        step.setBloomLevel(request.bloomLevel());
        step.setActivityType(request.activityType());
        step.setStepOrder(request.stepOrder());
        step.setResourceUrl(request.resourceUrl());

        CourseStep saved = courseStepRepository.save(step);
        activityLogService.log(user, course, "COURSE_STEP_CREATED", Map.of(
                "stepId", saved.getId(),
                "bloomLevel", saved.getBloomLevel().name(),
                "activityType", saved.getActivityType().name()
        ));
        return toStepMap(saved);
    }

    @GetMapping("/{courseId}/bloom-coverage")
    public Map<String, Object> bloomCoverage(@PathVariable Long courseId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);
        if (!course.isPublished() && !accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course is not published");
        }

        List<CourseStep> steps = courseStepRepository.findByCourseOrderByStepOrderAsc(course);
        Map<BloomLevel, Integer> coverage = new EnumMap<>(BloomLevel.class);
        for (BloomLevel level : BloomLevel.values()) {
            coverage.put(level, 0);
        }
        for (CourseStep step : steps) {
            coverage.put(step.getBloomLevel(), coverage.get(step.getBloomLevel()) + 1);
        }

        List<String> missingLevels = coverage.entrySet().stream()
                .filter(e -> e.getValue() == 0)
                .map(e -> e.getKey().name())
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("courseId", courseId);
        result.put("coverage", coverage);
        result.put("hasBalancedBloomFlow", missingLevels.isEmpty());
        result.put("missingLevels", missingLevels);
        result.put("guidance", missingLevels.isEmpty()
                ? "Course covers all Bloom levels."
                : "Add activities for missing levels to avoid passive content-only learning.");
        return result;
    }

    @PostMapping("/{courseId}/enroll")
    public Map<String, Object> enroll(@PathVariable Long courseId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can enroll themselves");
        }
        if (!course.isPublished()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot enroll in unpublished course");
        }

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(user, course).orElseGet(() -> {
            Enrollment e = new Enrollment();
            e.setCourse(course);
            e.setStudent(user);
            e.setStatus(EnrollmentStatus.ACTIVE);
            e.setProgressPercent(0);
            return enrollmentRepository.save(e);
        });

        activityLogService.log(user, course, "COURSE_ENROLLED", Map.of("enrollmentId", enrollment.getId()));
        return toEnrollmentMap(enrollment);
    }

    @GetMapping("/{courseId}/enrollments")
    public List<Map<String, Object>> listEnrollments(@PathVariable Long courseId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);

        if (!accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course owner or admin can view enrollments");
        }

        return enrollmentRepository.findByCourse(course).stream().map(this::toEnrollmentMap).toList();
    }

    @PostMapping("/{courseId}/progress")
    public Map<String, Object> updateProgress(@PathVariable Long courseId,
                                              @RequestParam int completedSteps,
                                              Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!accessControlService.isStudent(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can update progress");
        }

        Course course = getCourseOrThrow(courseId);
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(user, course)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));

        int totalSteps = courseStepRepository.findByCourseOrderByStepOrderAsc(course).size();
        int safeCompleted = Math.max(0, completedSteps);
        int progress = totalSteps == 0 ? 0 : (int) Math.min(100, Math.round((safeCompleted * 100.0) / totalSteps));

        enrollment.setProgressPercent(progress);
        if (progress >= 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
        }
        Enrollment saved = enrollmentRepository.save(enrollment);

        activityLogService.log(user, course, "COURSE_PROGRESS_UPDATED", Map.of(
                "completedSteps", safeCompleted,
                "totalSteps", totalSteps,
                "progressPercent", progress
        ));

        return toEnrollmentMap(saved);
    }

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private Map<String, Object> toCourseMap(Course course, User viewer) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", course.getId());
        map.put("title", course.getTitle());
        map.put("description", course.getDescription());
        map.put("published", course.isPublished());
        map.put("createdAt", course.getCreatedAt());
        map.put("instructor", Map.of(
                "id", course.getInstructor().getId(),
                "name", course.getInstructor().getFullName(),
                "email", course.getInstructor().getEmail()
        ));

        enrollmentRepository.findByStudentAndCourse(viewer, course).ifPresent(e -> map.put("enrollment", toEnrollmentMap(e)));

        return map;
    }

    private Map<String, Object> toStepMap(CourseStep step) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", step.getId());
        map.put("title", step.getTitle());
        map.put("description", step.getDescription());
        map.put("bloomLevel", step.getBloomLevel());
        map.put("activityType", step.getActivityType());
        map.put("stepOrder", step.getStepOrder());
        map.put("resourceUrl", step.getResourceUrl());
        return map;
    }

    private Map<String, Object> toEnrollmentMap(Enrollment enrollment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", enrollment.getId());
        map.put("courseId", enrollment.getCourse().getId());
        map.put("studentId", enrollment.getStudent().getId());
        map.put("status", enrollment.getStatus());
        map.put("progressPercent", enrollment.getProgressPercent());
        return map;
    }
}
