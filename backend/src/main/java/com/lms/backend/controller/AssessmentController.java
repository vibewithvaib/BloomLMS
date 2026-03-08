package com.lms.backend.controller;

import com.lms.backend.dto.AssignmentRequest;
import com.lms.backend.dto.PeerReviewRequest;
import com.lms.backend.dto.QuizAttemptRequest;
import com.lms.backend.dto.QuizQuestionRequest;
import com.lms.backend.dto.QuizRequest;
import com.lms.backend.dto.SubmissionRequest;
import com.lms.backend.model.Assignment;
import com.lms.backend.model.Course;
import com.lms.backend.model.PeerReview;
import com.lms.backend.model.Quiz;
import com.lms.backend.model.QuizAttempt;
import com.lms.backend.model.QuizQuestion;
import com.lms.backend.model.Submission;
import com.lms.backend.model.User;
import com.lms.backend.repository.AssignmentRepository;
import com.lms.backend.repository.CourseRepository;
import com.lms.backend.repository.PeerReviewRepository;
import com.lms.backend.repository.QuizAttemptRepository;
import com.lms.backend.repository.QuizQuestionRepository;
import com.lms.backend.repository.QuizRepository;
import com.lms.backend.repository.SubmissionRepository;
import com.lms.backend.service.AccessControlService;
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
public class AssessmentController {

    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final PeerReviewRepository peerReviewRepository;
    private final AccessControlService accessControlService;
    private final ActivityLogService activityLogService;

    @GetMapping("/courses/{courseId}/quizzes")
    public List<Map<String, Object>> listQuizzes(@PathVariable Long courseId) {
        Course course = getCourseOrThrow(courseId);
        return quizRepository.findByCourse(course).stream().map(this::toQuizMap).toList();
    }

    @PostMapping("/courses/{courseId}/quizzes")
    public Map<String, Object> createQuiz(@PathVariable Long courseId,
                                          @Valid @RequestBody QuizRequest request,
                                          Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);
        if (!accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course owner or admin can create quiz");
        }

        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setTitle(request.title());
        quiz.setInstructions(request.instructions());
        Quiz saved = quizRepository.save(quiz);

        activityLogService.log(user, course, "QUIZ_CREATED", Map.of("quizId", saved.getId()));
        return toQuizMap(saved);
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public List<Map<String, Object>> listQuizQuestions(@PathVariable Long quizId) {
        Quiz quiz = getQuizOrThrow(quizId);
        return quizQuestionRepository.findByQuiz(quiz).stream().map(this::toQuestionMapWithoutAnswer).toList();
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public Map<String, Object> addQuizQuestion(@PathVariable Long quizId,
                                               @Valid @RequestBody QuizQuestionRequest request,
                                               Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Quiz quiz = getQuizOrThrow(quizId);
        if (!accessControlService.canManageCourse(user, quiz.getCourse())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course owner or admin can add questions");
        }

        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setPrompt(request.prompt());
        question.setOptionA(request.optionA());
        question.setOptionB(request.optionB());
        question.setOptionC(request.optionC());
        question.setOptionD(request.optionD());
        question.setCorrectOption(request.correctOption().toUpperCase());

        QuizQuestion saved = quizQuestionRepository.save(question);
        activityLogService.log(user, quiz.getCourse(), "QUIZ_QUESTION_ADDED", Map.of("questionId", saved.getId()));
        return toQuestionMapWithAnswer(saved);
    }

    @PostMapping("/quizzes/{quizId}/attempts")
    public Map<String, Object> submitQuiz(@PathVariable Long quizId,
                                          @RequestBody QuizAttemptRequest request,
                                          Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Quiz quiz = getQuizOrThrow(quizId);

        List<QuizQuestion> questions = quizQuestionRepository.findByQuiz(quiz);
        int total = questions.size();
        int score = 0;
        Map<Long, String> answers = request.answers() == null ? Map.of() : request.answers();

        for (QuizQuestion question : questions) {
            String studentAnswer = answers.get(question.getId());
            if (studentAnswer != null && question.getCorrectOption().equalsIgnoreCase(studentAnswer.trim())) {
                score++;
            }
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(user);
        attempt.setScore(score);
        attempt.setTotal(total);
        QuizAttempt saved = quizAttemptRepository.save(attempt);

        activityLogService.log(user, quiz.getCourse(), "QUIZ_ATTEMPT_SUBMITTED", Map.of(
                "quizId", quizId,
                "score", score,
                "total", total
        ));

        Map<String, Object> map = new HashMap<>();
        map.put("attemptId", saved.getId());
        map.put("score", score);
        map.put("total", total);
        map.put("percentage", total == 0 ? 0 : Math.round((score * 100.0) / total));
        return map;
    }

    @GetMapping("/courses/{courseId}/assignments")
    public List<Map<String, Object>> listAssignments(@PathVariable Long courseId) {
        Course course = getCourseOrThrow(courseId);
        return assignmentRepository.findByCourse(course).stream().map(this::toAssignmentMap).toList();
    }

    @PostMapping("/courses/{courseId}/assignments")
    public Map<String, Object> createAssignment(@PathVariable Long courseId,
                                                @Valid @RequestBody AssignmentRequest request,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);
        if (!accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course owner or admin can create assignment");
        }

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(request.title());
        assignment.setInstructions(request.instructions());
        assignment.setDueDate(request.dueDate());
        Assignment saved = assignmentRepository.save(assignment);

        activityLogService.log(user, course, "ASSIGNMENT_CREATED", Map.of("assignmentId", saved.getId()));
        return toAssignmentMap(saved);
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public Map<String, Object> submitAssignment(@PathVariable Long assignmentId,
                                                @Valid @RequestBody SubmissionRequest request,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Assignment assignment = getAssignmentOrThrow(assignmentId);

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(user);
        submission.setContent(request.content());
        Submission saved = submissionRepository.save(submission);

        activityLogService.log(user, assignment.getCourse(), "ASSIGNMENT_SUBMITTED", Map.of(
                "assignmentId", assignmentId,
                "submissionId", saved.getId()
        ));
        return toSubmissionMap(saved);
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public List<Map<String, Object>> listSubmissions(@PathVariable Long assignmentId,
                                                     Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Assignment assignment = getAssignmentOrThrow(assignmentId);

        List<Submission> submissions = submissionRepository.findByAssignment(assignment);
        if (accessControlService.canManageCourse(user, assignment.getCourse())) {
            return submissions.stream().map(this::toSubmissionMap).toList();
        }

        return submissions.stream()
                .filter(submission -> submission.getStudent().getId().equals(user.getId()))
                .map(this::toSubmissionMap)
                .toList();
    }

    @PostMapping("/submissions/{submissionId}/reviews")
    public Map<String, Object> peerReview(@PathVariable Long submissionId,
                                          @Valid @RequestBody PeerReviewRequest request,
                                          Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Submission submission = getSubmissionOrThrow(submissionId);
        if (submission.getStudent().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot review your own submission");
        }

        PeerReview review = new PeerReview();
        review.setSubmission(submission);
        review.setReviewer(user);
        review.setFeedback(request.feedback());
        review.setScore(request.score());
        PeerReview saved = peerReviewRepository.save(review);

        activityLogService.log(user, submission.getAssignment().getCourse(), "PEER_REVIEW_SUBMITTED", Map.of(
                "submissionId", submissionId,
                "reviewId", saved.getId(),
                "score", request.score()
        ));

        return toReviewMap(saved);
    }

    @GetMapping("/submissions/{submissionId}/reviews")
    public List<Map<String, Object>> reviews(@PathVariable Long submissionId) {
        Submission submission = getSubmissionOrThrow(submissionId);
        return peerReviewRepository.findBySubmission(submission).stream().map(this::toReviewMap).toList();
    }

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private Quiz getQuizOrThrow(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }

    private Assignment getAssignmentOrThrow(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
    }

    private Submission getSubmissionOrThrow(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
    }

    private Map<String, Object> toQuizMap(Quiz quiz) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", quiz.getId());
        map.put("courseId", quiz.getCourse().getId());
        map.put("title", quiz.getTitle());
        map.put("instructions", quiz.getInstructions());
        map.put("questionCount", quizQuestionRepository.findByQuiz(quiz).size());
        return map;
    }

    private Map<String, Object> toQuestionMapWithoutAnswer(QuizQuestion question) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", question.getId());
        map.put("prompt", question.getPrompt());
        map.put("optionA", question.getOptionA());
        map.put("optionB", question.getOptionB());
        map.put("optionC", question.getOptionC());
        map.put("optionD", question.getOptionD());
        return map;
    }

    private Map<String, Object> toQuestionMapWithAnswer(QuizQuestion question) {
        Map<String, Object> map = toQuestionMapWithoutAnswer(question);
        map.put("correctOption", question.getCorrectOption());
        return map;
    }

    private Map<String, Object> toAssignmentMap(Assignment assignment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", assignment.getId());
        map.put("courseId", assignment.getCourse().getId());
        map.put("title", assignment.getTitle());
        map.put("instructions", assignment.getInstructions());
        map.put("dueDate", assignment.getDueDate());
        return map;
    }

    private Map<String, Object> toSubmissionMap(Submission submission) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", submission.getId());
        map.put("assignmentId", submission.getAssignment().getId());
        map.put("content", submission.getContent());
        map.put("grade", submission.getGrade());
        map.put("student", Map.of(
                "id", submission.getStudent().getId(),
                "name", submission.getStudent().getFullName()
        ));
        map.put("createdAt", submission.getCreatedAt());
        return map;
    }

    private Map<String, Object> toReviewMap(PeerReview review) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", review.getId());
        map.put("submissionId", review.getSubmission().getId());
        map.put("feedback", review.getFeedback());
        map.put("score", review.getScore());
        map.put("reviewer", Map.of(
                "id", review.getReviewer().getId(),
                "name", review.getReviewer().getFullName()
        ));
        map.put("createdAt", review.getCreatedAt());
        return map;
    }
}
