package com.lms.backend.repository;

import com.lms.backend.model.Course;
import com.lms.backend.model.ForumThread;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    List<ForumThread> findByCourse(Course course);
}
