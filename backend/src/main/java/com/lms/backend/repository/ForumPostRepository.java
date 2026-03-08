package com.lms.backend.repository;

import com.lms.backend.model.ForumPost;
import com.lms.backend.model.ForumThread;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByThreadOrderByCreatedAtAsc(ForumThread thread);
}
