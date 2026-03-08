# REST API Design

Base URL: `/api`

## Auth

- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/health`

## Users

- `GET /users/me`
- `GET /users` (admin)
- `GET /users/instructors`

## Courses

- `GET /courses`
- `POST /courses` (instructor/admin)
- `GET /courses/{courseId}`
- `PATCH /courses/{courseId}/publish?published=true`
- `GET /courses/{courseId}/steps`
- `POST /courses/{courseId}/steps`
- `GET /courses/{courseId}/bloom-coverage`
- `POST /courses/{courseId}/enroll` (student)
- `GET /courses/{courseId}/enrollments`
- `POST /courses/{courseId}/progress?completedSteps=n`

## Collaboration

- `GET /courses/{courseId}/forum/threads`
- `POST /courses/{courseId}/forum/threads`
- `GET /forum/threads/{threadId}/posts`
- `POST /forum/threads/{threadId}/posts`
- `GET /courses/{courseId}/wiki`
- `POST /courses/{courseId}/wiki`
- `PUT /wiki/{pageId}`
- `GET /courses/{courseId}/projects`
- `POST /courses/{courseId}/projects`
- `GET /projects/{projectId}/members`
- `POST /projects/{projectId}/members`

## Assessment

- `GET /courses/{courseId}/quizzes`
- `POST /courses/{courseId}/quizzes`
- `GET /quizzes/{quizId}/questions`
- `POST /quizzes/{quizId}/questions`
- `POST /quizzes/{quizId}/attempts`
- `GET /courses/{courseId}/assignments`
- `POST /courses/{courseId}/assignments`
- `POST /assignments/{assignmentId}/submissions`
- `GET /assignments/{assignmentId}/submissions`
- `POST /submissions/{submissionId}/reviews`
- `GET /submissions/{submissionId}/reviews`

## Reflection

- `GET /courses/{courseId}/reflections`
- `POST /courses/{courseId}/reflections`
- `GET /reflections/me`

## Analytics

- `GET /analytics/me`
- `GET /analytics/courses/{courseId}/overview`
