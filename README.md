# Employee Management System (EMS)

### Spring Boot Management Platform
**Project for portfolio purposes**

---

### Project Overview

**Employee Management System (EMS)** is a junior-level backend solution designed to streamline management processes. The application focuses on a clear separation between **Technical User Accounts** (authentication) and **Employee Profiles** (business data).

The system implements high-security standards using **JWT** and role-based access control. It is built to demonstrate clean architecture, DTO patterns, and production-ready database management.

---

### Technologies Used

#### Backend
* **Java & Spring Boot** – Core framework for the RESTful API
* **Spring Security** – Robust security layer for authentication and authorization
* **JWT (JSON Web Tokens)** – Stateless session management
* **Spring Data JPA** – Database abstraction and ORM
* **PostgreSQL** – Relational database for persistent storage

#### Infrastructure & Tools
* **Docker** – Containerized PostgreSQL environment
* **Maven** – Dependency management and build tool

---

### Key Features

#### Authentication & Security
* **JWT-Based Auth:** Secure, stateless login flow.
* **Role-Based Access Control (RBAC):** Distinct permissions for `ADMIN` and `EMPLOYEE` roles.
* **Secure Data Access:** Dedicated `/me` endpoint to fetch the currently logged-in user's profile securely.

#### Employee Management
* **Admin Dashboard:** Full CRUD operations (Create, Read, Update, Delete) for employee records.
* **Profile Logic:** 1:1 relationship between System Users and Employee Profiles to ensure data integrity.
* **Data Validation:** Strict DTO-based validation for all incoming requests.

---

### Backend Architecture

* **RESTful Design:** Clean, resource-oriented API structure.
* **Separated Concerns:** Clear split between Controllers, Services, Repositories, and Security Filters.
* **Database Versioning:** Ready for migration tools.
