# Library Management System

Full-stack library management application with **Spring Boot + MySQL** backend and **React** frontend.

## Features

- Secure login/signup with JWT and BCrypt password hashing
- Role-based access (`LIBRARIAN`, `MEMBER`)
- Book, author, member, and lending management
- Business rules:
  - Unique ISBN per book
  - Book issue only when available
  - Max 5 active loans per member
  - 14-day loan period
- Notifications for due/overdue books (manual + scheduled)
- API rate limiting, logging, actuator health/metrics
- Dockerized deployment and CI pipeline
- API test coverage enforced with JaCoCo (>80%)

## Project Structure

- `backend/` – Spring Boot REST API
- `frontend/` – React (Vite) responsive UI
- `docker-compose.yml` – MySQL + API + UI

## Quick Start (Local)

### 1) MySQL

Create DB and update credentials in `backend/src/main/resources/application.yml` if needed.

### 2) Backend

```bash
cd backend
mvn spring-boot:run
```

Default librarian account (auto-seeded):

- Username: `librarian`
- Password: `librarian123`

### 3) Frontend

```bash
cd frontend
npm install
npm run dev
```

Open: `http://localhost:5173`

## Docker

```bash
docker compose up --build
```

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- MySQL: `localhost:3306`

## API Endpoints

| Feature | Endpoint |
|---|---|
| Register | `POST /api/auth/register` |
| Login | `POST /api/auth/login` |
| Logout | `POST /api/auth/logout` |
| Books | `GET/POST/PUT/DELETE /api/books` |
| Authors | `GET/POST/PUT/DELETE /api/authors` |
| Members | `GET/POST/PUT/DELETE /api/members` |
| Lendings | `GET/POST /api/lendings`, `PUT /api/lendings/{id}` |
| Member history | `GET /api/lendings/my` |
| Notifications | `POST /api/notifications`, `GET /api/notifications/my` |

## Testing

```bash
cd backend
mvn verify
```

JaCoCo report: `backend/target/site/jacoco/index.html`

## Azure/AWS Notes

- Deploy backend container to **Azure App Service** or **AWS ECS**
- Deploy frontend container to static hosting (**Azure Static Web Apps** / **S3 + CloudFront**)
- Use managed MySQL (**Azure Database for MySQL** / **Amazon RDS**)
- Store secrets (`OPENAI`/DB/JWT) in **Azure Key Vault** or **AWS Secrets Manager**
