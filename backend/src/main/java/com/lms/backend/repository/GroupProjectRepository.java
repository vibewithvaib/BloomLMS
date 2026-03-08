package com.lms.backend.repository;

import com.lms.backend.model.Course;
import com.lms.backend.model.GroupProject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupProjectRepository extends JpaRepository<GroupProject, Long> {
    List<GroupProject> findByCourse(Course course);
}
