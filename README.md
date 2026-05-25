# Personal Finance Manager — Backend API

A production-style RESTful backend application for managing personal finances, built using **Spring Boot 3**, **Java 17**, **Spring Security**, **Spring Data JPA**, **Hibernate**, and **H2 Database**.

The system allows users to:
- Track income and expenses
- Manage custom financial categories
- Create savings goals
- Generate monthly and yearly reports
- Securely manage personal financial data using session-based authentication

---

# Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Security | Spring Security |
| ORM | Spring Data JPA + Hibernate |
| Database | H2 Database |
| Build Tool | Maven |
| Testing | JUnit 5 + Mockito |

---

# Features

## Authentication & User Management
- User registration
- Login/logout using session-based authentication
- Secure cookie-based sessions
- User data isolation

## Transaction Management
- Create, update, delete transactions
- Date range filtering
- Category filtering
- Transactions sorted newest first
- Future dates validation
- Amount validation

## Category Management
- Default categories auto-seeded
- Custom category creation
- Duplicate prevention
- Prevent deletion of categories currently in use

## Savings Goals
- Create and manage goals
- Dynamic progress tracking
- Automatic savings calculations
- Remaining amount calculation

## Reports & Analytics
- Monthly reports
- Yearly reports
- Income/expense aggregation by category
- Net savings calculation

## Error Handling
- Global exception handling using `@ControllerAdvice`
- Consistent JSON error responses
- Proper HTTP status codes

---

# Project Structure

```text
src/main/java/com/adarsh/financemanager/
│
├── controller/       # REST Controllers
├── service/          # Business logic
├── repository/       # Database layer
├── dto/              # Request & Response DTOs
├── entity/           # JPA Entities
├── exception/        # Custom exceptions & handlers
├── security/         # Security configuration
├── config/           # App configuration
```

Architecture followed:

```text
Controller → Service → Repository
```

---

# Setup Instructions

## Prerequisites

- Java 17+
- Maven

---

## Clone Repository

```bash
git clone https://github.com/adarsh062/financial-manager.git
cd financial-manager
```

---

## Run Application

### Using Maven Wrapper

Linux/Mac:
```bash
./mvnw spring-boot:run
```

Windows:
```bash
mvnw.cmd spring-boot:run
```

---

Application runs on:

```text
http://localhost:8080
```

Base API URL:

```text
http://localhost:8080/api
```

---

# H2 Database Console

URL:
```text
http://localhost:8080/h2-console
```

Credentials:

```text
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password:
```

---

# Authentication Flow

1. Register user
2. Login user
3. Session cookie (`JSESSIONID`) generated
4. Use session automatically for protected APIs
5. Logout invalidates session

---

# API Endpoints

# Auth APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register user |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |

---

# Category APIs

| Method | Endpoint |
|---|---|
| GET | `/api/categories` |
| POST | `/api/categories` |
| DELETE | `/api/categories/{name}` |

---

# Transaction APIs

| Method | Endpoint |
|---|---|
| POST | `/api/transactions` |
| GET | `/api/transactions` |
| PUT | `/api/transactions/{id}` |
| DELETE | `/api/transactions/{id}` |

### Transaction Filters

```text
?startDate=2024-01-01
?endDate=2024-01-31
?category=Salary
```

---

# Goal APIs

| Method | Endpoint |
|---|---|
| POST | `/api/goals` |
| GET | `/api/goals` |
| GET | `/api/goals/{id}` |
| PUT | `/api/goals/{id}` |
| DELETE | `/api/goals/{id}` |

---

# Report APIs

| Method | Endpoint |
|---|---|
| GET | `/api/reports/monthly/{year}/{month}` |
| GET | `/api/reports/yearly/{year}` |

---

# Sample Requests

## Register User

```http
POST /api/auth/register
```

```json
{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "Adarsh Kumar",
  "phoneNumber": "+919876543210"
}
```

---

## Create Transaction

```http
POST /api/transactions
```

```json
{
  "amount": 5000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary"
}
```

---

## Create Goal

```http
POST /api/goals
```

```json
{
  "goalName": "Emergency Fund",
  "targetAmount": 10000,
  "targetDate": "2027-01-01"
}
```

---

# Example Report Response

```json
{
  "month": 1,
  "year": 2024,
  "totalIncome": {
    "Salary": 5000.00
  },
  "totalExpenses": {
    "Food": 400.00
  },
  "netSavings": 4600.00
}
```

---

# Default Categories

## Income
- Salary

## Expense
- Food
- Rent
- Transportation
- Entertainment
- Healthcare
- Utilities

---

# HTTP Status Codes

| Status | Description |
|---|---|
| 200 | Success |
| 201 | Resource created |
| 400 | Validation error |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Resource not found |
| 409 | Conflict |

---

# Validation Rules

## Transactions
- Amount must be positive
- Date cannot be future date
- Category must exist

## Goals
- Target amount must be positive
- Target date must be future date

## Categories
- Duplicate custom categories not allowed
- Categories in use cannot be deleted

---

# Exception Handling

Global exception handling implemented using:

```java
@RestControllerAdvice
```

Handled exceptions:
- Validation errors
- Resource not found
- Unauthorized access
- Duplicate resources
- Invalid request formats

---

# Testing

- JUnit 5
- Mockito
- Service layer unit tests
- Mocked dependencies
- 80%+ code coverage target

---

# Deployment

Live Deployment:

https://financial-manager-1.onrender.com/api

GitHub Repository:

https://github.com/adarsh062/financial-manager

---

# Official E2E Test Result

```text
Total Tests Executed: 86
Tests Passed: 86
Tests Failed: 0
Success Rate: 100%
```

All official assignment test cases pass successfully.

---

# Design Decisions

- Session-based authentication instead of JWT
- DTO-based API architecture
- Layered architecture for maintainability
- Global exception handling for consistent responses
- User-level data isolation for security
- Dynamic report and goal calculations

---

# Future Improvements

- JWT authentication
- Swagger/OpenAPI documentation
- PostgreSQL integration
- Docker Compose support
- CI/CD pipeline
- Frontend integration

---

# Author

Adarsh