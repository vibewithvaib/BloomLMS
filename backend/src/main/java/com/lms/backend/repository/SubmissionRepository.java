package com.lms.backend.repository;

import com.lms.backend.model.Assignment;
import com.lms.backend.model.Submission;
import com.lms.backend.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignment(Assignment assignment);
    List<Submission> findByStudent(User student);
}
