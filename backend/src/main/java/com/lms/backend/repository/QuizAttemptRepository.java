package com.lms.backend.repository;

import com.lms.backend.model.Quiz;
import com.lms.backend.model.QuizAttempt;
import com.lms.backend.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuiz(Quiz quiz);
    List<QuizAttempt> findByStudent(User student);
}
