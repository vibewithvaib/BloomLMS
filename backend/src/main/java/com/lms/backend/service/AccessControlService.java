package com.lms.backend.service;

import com.lms.backend.model.Course;
import com.lms.backend.model.Role;
import com.lms.backend.model.User;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean isInstructor(User user) {
        return user.getRole() == Role.INSTRUCTOR;
    }

    public boolean isStudent(User user) {
        return user.getRole() == Role.STUDENT;
    }

    public boolean canManageCourse(User user, Course course) {
        return isAdmin(user) || (isInstructor(user) && course.getInstructor().getId().equals(user.getId()));
    }
}
