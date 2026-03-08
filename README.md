# Bloom LMS (React + Spring Boot)

A full-stack Learning Management System, designed to close the gap between pedagogical intent and real usage by actively guiding courses across Bloom's taxonomy.

## Stack

- Frontend: React + Vite + Axios + React Router
- Backend: Spring Boot 3, Spring Security, JWT, JPA
- Database: PostgreSQL or MySQL (configurable)

## Key Pedagogical Focus

This LMS enforces a Bloom-guided learning path and collaborative knowledge building:

- Remember: resources
- Understand: quizzes
- Apply: assignments
- Analyze: forums
- Evaluate: reflection blogs/journals
- Create: collaborative wiki/group projects

## Modules Implemented

- User Management: registration, JWT login, RBAC, profile APIs
- Course Management: course creation, publication, learning steps/paths
- Collaborative Learning: forum threads/posts, wiki pages, group projects
- Assessment: quizzes, auto-scored attempts, assignments, submissions, peer review
- Reflection: blogs/journals/discussion summaries
- Analytics: learner analytics, course engagement dashboards, Bloom coverage metrics

## Monorepo Structure

- `backend/` Spring Boot REST API
- `frontend/` React client
- `docs/` architecture + schema + API + workflow documentation

## Quick Start

### 1) Backend

```bash
cd backend
mvn spring-boot:run
```
path: localhost:8081

### 2) Frontend

```bash
cd frontend
npm install
npm run dev
```


## Seeded Demo Accounts

- Instructor: `instructor@lms.local` / `Instructor@123`
- Student: `student@lms.local` / `Student@123`
- Admin: `admin@lms.local` / `Admin@123`

## Core Docs

- [Architecture](docs/ARCHITECTURE.md)
- [Database Schema](docs/DB_SCHEMA.md)
- [REST API Design](docs/API_DESIGN.md)
- [Example Collaborative Workflow](docs/WORKFLOW.md)
