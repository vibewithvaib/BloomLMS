# Database Schema

## Core Tables

- `users`
  - id, full_name, email (unique), password, role, enabled, timestamps
- `courses`
  - id, title, description, instructor_id -> users, published, timestamps
- `course_steps`
  - id, course_id -> courses, title, description, bloom_level, activity_type, step_order, resource_url, timestamps
- `enrollments`
  - id, student_id -> users, course_id -> courses, status, progress_percent, timestamps

## Collaboration Tables

- `forum_threads`
  - id, course_id -> courses, created_by -> users, title, prompt, timestamps
- `forum_posts`
  - id, thread_id -> forum_threads, author_id -> users, parent_post_id -> forum_posts (nullable), content, timestamps
- `wiki_pages`
  - id, course_id -> courses, updated_by -> users, title, content, timestamps
- `group_projects`
  - id, course_id -> courses, name, description, due_date, timestamps
- `project_memberships`
  - id, project_id -> group_projects, student_id -> users, timestamps

## Assessment Tables

- `quizzes`
  - id, course_id -> courses, title, instructions, timestamps
- `quiz_questions`
  - id, quiz_id -> quizzes, prompt, option_a/b/c/d, correct_option, timestamps
- `quiz_attempts`
  - id, quiz_id -> quizzes, student_id -> users, score, total, timestamps
- `assignments`
  - id, course_id -> courses, title, instructions, due_date, timestamps
- `submissions`
  - id, assignment_id -> assignments, student_id -> users, content, grade, timestamps
- `peer_reviews`
  - id, submission_id -> submissions, reviewer_id -> users, feedback, score, timestamps

## Reflection + Analytics Tables

- `reflection_entries`
  - id, course_id -> courses, student_id -> users, type, title, content, timestamps
- `activity_logs`
  - id, user_id -> users, course_id -> courses (nullable), action_type, metadata, timestamps

## Index Suggestions

- users(email)
- courses(instructor_id)
- course_steps(course_id, step_order)
- enrollments(student_id, course_id)
- activity_logs(course_id, created_at)
