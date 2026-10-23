# Employee Management System (EMS)

**Spring Boot REST API with JWT authentication, role-based access control and AWS S3 file storage, plus a minimal React client.**
Portfolio project.

---

## Project overview

EMS is a backend application for managing company employees. It keeps a clear separation between
**technical user accounts** (login, password, role) and **employee profiles** (business data).

An administrator creates employee accounts. The system generates the login and a one-time password,
which the administrator passes on to the employee. Employees can sign in and view their own profile.
Administrators can list, update and delete employees and manage their profile pictures stored in AWS S3.

The `client/` folder contains a deliberately small React app. Its only purpose is to demonstrate the API in a browser.

---

## Tech stack

**Backend** (`employee-management-system/`)
* **Java 21**, **Spring Boot 3.5**
* **Spring Security** + **JWT**
* **Spring Data JPA / Hibernate** with **PostgreSQL 18**
* **AWS SDK v2 (S3)**
* **JUnit 5, Mockito, MockMvc, H2**

**Frontend** (`client/`)
* **React 19** + **Vite**

**Infrastructure**
* **Docker / Docker Compose**
* **Maven**

---

## Key features

### Authentication & security
* **JWT login:** `POST /auth/authenticate` returns a token (valid for 24 h) and the user's role.
* **Token refresh:** `POST /auth/refresh` issues a new token. It also accepts a token that expired up to 7 days ago,
  as long as its signature is valid.
* **Role-based access control:** `ADMIN` manages employees. `EMPLOYEE` can only read their own profile (`/me`).

### Employee management
* **Create:** the login is generated from the name (diacritics removed, e.g. *Łukasz Żółć* → `lukaszzolc_X7k2a`),
  together with a random 12-character password returned **once**.
* **Read / update / delete:** an administrator cannot delete their own account.
* **Profile images:** upload to S3. The old image is removed only after the new one is stored.

---

## Running locally

### Prerequisites

```bash
git clone git@github.com:maksluczak/employee-management.git
cd employee-management
```

### Option A: with Docker

```bash
docker compose up -d --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api/v1 |
| PostgreSQL | `localhost:5332`, database `company_db`, user `maksluczak` / `S3cret` |

Sign in with the default administrator: **`admin` / `admin123`**.

```bash
docker compose ps
docker compose down
docker compose down -v
```

### Option B: backend and frontend from the IDE / terminal

Use this when you work on the code. Only the database runs in Docker.

```bash
# 1. database
docker compose up -d db

# 2. backend
cd employee-management-system
./mvnw spring-boot:run

# 3. frontend
cd client
npm install
npm run dev
```

### Profile images (AWS S3)

Everything except image upload/download works without AWS. Without credentials, uploading an image returns
`502 File storage is unavailable`.

To enable images:
1. Create an S3 bucket and an IAM user with `s3:PutObject`, `s3:GetObject` and `s3:DeleteObject` on it.
2. Configure credentials in `~/.aws/credentials`. Docker Compose mounts this folder into the backend container as read-only.
3. Set your bucket name and region.

---

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5332/company_db` | JDBC URL (`db:5432` inside Docker Compose) |
| `DB_USERNAME` / `DB_PASSWORD` | `maksluczak` / `S3cret` | Database credentials |
| `JWT_SECRET` | development key | Base64-encoded key for signing tokens |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin123` | First administrator, created only if no admin exists |
| `AWS_REGION` | `eu-north-1` | S3 region |
| `AWS_S3_BUCKET` | `fs-maksluczak-client-test` | Bucket for profile images |

The database schema is managed by Hibernate (`ddl-auto=update`). Tables are created and updated on startup,
and data is kept between restarts.

---

## API

Base URL: `http://localhost:8080/api/v1`. All endpoints except `/auth/**` require the header
`Authorization: Bearer <token>`.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/auth/authenticate` | public | Log in, returns `{ token, role }` |
| POST | `/auth/refresh` | public (Bearer token) | New token, also for a token expired up to 7 days ago |
| GET | `/employees` | ADMIN | List all employees |
| GET | `/employees/{id}` | ADMIN | Employee details |
| POST | `/employees` | ADMIN | Create employee, `201` with `{ id, username, email, password }` |
| PUT | `/employees/{id}` | ADMIN | Change position, `204` |
| DELETE | `/employees/{id}` | ADMIN | Delete employee and their account, `204` |
| GET | `/employees/{id}/profile-image` | ADMIN | Download profile image |
| POST | `/employees/{id}/profile-image` | ADMIN | Upload image (`multipart/form-data`, field `file`), `204` |
| GET | `/employees/me` | ADMIN, EMPLOYEE | Own profile |
| GET | `/employees/me/profile-image` | ADMIN, EMPLOYEE | Own profile image |

---

## Tests

```bash
cd employee-management-system
./mvnw test
```

24 tests use an in-memory **H2** database. S3 is mocked.

| Test class | Type | What it covers |
|---|---|---|
| `JwtServiceTest` | unit | token generation, expiry, refresh window, foreign signatures |
| `AuthenticationServiceTest` | unit | successful and failed login |
| `EmployeeServiceTest` | unit | generated credentials, 404, image upload order, file type validation, self-deletion guard |
| `AuthenticationControllerTest` | integration | login `200` / `401`, token refresh |
| `EmployeeControllerTest` | integration | `401` without token, `403` for employees on admin endpoints, validation `400`, `404`, CRUD, image upload |
| `MainTests` | context | application context starts |

---

## Project structure

```
employee-management/
├── docker-compose.yml
├── employee-management-system/
│   ├── Dockerfile
│   └── src/main/java/io/github/maksluczak/ems/
│       ├── auth/
│       ├── config/
│       ├── employee/
│       ├── exception/
│       ├── s3/
│       └── user/
└── client/
    ├── Dockerfile
    └── src/
        ├── api.js
        ├── App.jsx
        └── pages/
```
