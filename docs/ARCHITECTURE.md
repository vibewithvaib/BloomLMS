# System Architecture

## Overview

Client-server LMS with modular backend domains and role-specific frontend dashboards.

## Logical Layers

1. Presentation Layer (React)
- Role-based dashboards
- Course designer
- Course workspace (forum/wiki/assessment/reflection)
- Analytics screens

2. API Layer (Spring Boot Controllers)
- AuthController
- UserController
- CourseController
- CollaborationController
- AssessmentController
- ReflectionController
- AnalyticsController

3. Domain/Service Layer
- AccessControlService for course-level permission checks
- ActivityLogService for engagement telemetry
- BootstrapDataLoader for seeded demo flow

4. Persistence Layer
- JPA entities for users, courses, learning steps, enrollments, collaboration, assessments, reflections, logs
- Spring Data repositories

## Security Architecture

- JWT authentication (Bearer token)
- Stateless sessions
- RBAC roles: `ADMIN`, `INSTRUCTOR`, `STUDENT`
- Method and business-level authorization for course ownership and instructor functions

## Scalability Notes

- Modular controllers and repositories support service extraction later
- Activity logging supports future event-stream analytics
- Bloom coverage and engagement metrics are computed from durable entities
