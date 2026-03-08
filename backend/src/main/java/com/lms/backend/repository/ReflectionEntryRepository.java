package com.lms.backend.repository;

import com.lms.backend.model.Course;
import com.lms.backend.model.ReflectionEntry;
import com.lms.backend.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReflectionEntryRepository extends JpaRepository<ReflectionEntry, Long> {
    List<ReflectionEntry> findByCourse(Course course);
    List<ReflectionEntry> findByStudent(User student);
}
