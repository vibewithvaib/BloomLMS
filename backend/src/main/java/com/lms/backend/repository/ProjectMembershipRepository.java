package com.lms.backend.repository;

import com.lms.backend.model.GroupProject;
import com.lms.backend.model.ProjectMembership;
import com.lms.backend.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, Long> {
    List<ProjectMembership> findByProject(GroupProject project);
    List<ProjectMembership> findByStudent(User student);
}
