package com.lms.backend.config;

import com.lms.backend.model.ActivityType;
import com.lms.backend.model.BloomLevel;
import com.lms.backend.model.Course;
import com.lms.backend.model.CourseStep;
import com.lms.backend.model.Role;
import com.lms.backend.model.User;
import com.lms.backend.repository.CourseRepository;
import com.lms.backend.repository.CourseStepRepository;
import com.lms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BootstrapDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseStepRepository courseStepRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = ensureUser("System Admin", "admin@lms.local", "Admin@123", Role.ADMIN);
        User instructor = ensureUser("Demo Instructor", "instructor@lms.local", "Instructor@123", Role.INSTRUCTOR);
        ensureUser("Demo Student", "student@lms.local", "Student@123", Role.STUDENT);

        if (courseRepository.count() == 0) {
            Course course = new Course();
            course.setTitle("Database Design Studio");
            course.setDescription("Bloom-guided course that moves learners from recall to collaborative creation.");
            course.setInstructor(instructor != null ? instructor : admin);
            course.setPublished(true);
            Course saved = courseRepository.save(course);

            createStep(saved, 1, BloomLevel.REMEMBER, ActivityType.RESOURCE,
                    "Study Relational Modeling Basics",
                    "Read and watch the provided materials on entities, attributes, and relationships.",
                    "https://example.org/database-design-intro");
            createStep(saved, 2, BloomLevel.UNDERSTAND, ActivityType.QUIZ,
                    "Concept Check Quiz",
                    "Complete a concept quiz on keys, constraints, and normalization goals.",
                    null);
            createStep(saved, 3, BloomLevel.APPLY, ActivityType.ASSIGNMENT,
                    "Normalization Exercise",
                    "Apply 1NF-3NF rules to sample datasets and submit rationale.",
                    null);
            createStep(saved, 4, BloomLevel.ANALYZE, ActivityType.FORUM,
                    "Forum: Trade-offs in Schema Design",
                    "Discuss denormalization and indexing trade-offs with peers.",
                    null);
            createStep(saved, 5, BloomLevel.EVALUATE, ActivityType.BLOG,
                    "Reflection Blog Critique",
                    "Critique two alternative schema models and justify your recommendation.",
                    null);
            createStep(saved, 6, BloomLevel.CREATE, ActivityType.WIKI,
                    "Collaborative Wiki Artifact",
                    "Co-author a final wiki documenting a full database design.",
                    null);
        }
    }

    private User ensureUser(String fullName, String email, String rawPassword, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setEnabled(true);
            return userRepository.save(user);
        });
    }

    private void createStep(Course course,
                            int order,
                            BloomLevel level,
                            ActivityType type,
                            String title,
                            String description,
                            String resourceUrl) {
        CourseStep step = new CourseStep();
        step.setCourse(course);
        step.setStepOrder(order);
        step.setBloomLevel(level);
        step.setActivityType(type);
        step.setTitle(title);
        step.setDescription(description);
        step.setResourceUrl(resourceUrl);
        courseStepRepository.save(step);
    }
}
