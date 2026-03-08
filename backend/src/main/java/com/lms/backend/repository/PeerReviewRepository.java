package com.lms.backend.repository;

import com.lms.backend.model.PeerReview;
import com.lms.backend.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeerReviewRepository extends JpaRepository<PeerReview, Long> {
    List<PeerReview> findBySubmission(Submission submission);
}
