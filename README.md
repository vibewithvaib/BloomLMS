## Stack

- Frontend: React + Vite + Axios + React Router
- Backend: Spring Boot 3, Spring Security, JWT, JPA
- Database: PostgreSQL or MySQL (configurable)

## Key Pedagogical Focus

This LMS enforces a Bloom-guided learning path and collaborative knowledge building:


## Modules Implemented

- User Management
- Course Management
- Collaborative Learning
- Assessment
- Reflection
- Analytics

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
