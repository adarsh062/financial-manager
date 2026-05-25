# Personal Finance Manager — Backend API

A production-style RESTful backend for managing personal finances — built with **Spring Boot 3**, **Java 17**, **Spring Security (session-based)**, **Spring Data JPA**, **Hibernate**, and **H2 Database** (dev) / **PostgreSQL** (prod).

---

## Features

- **Authentication**: Register, Login, Logout with session-based security
- **Categories**: Default + custom categories with soft-delete and usage guard
- **Transactions**: Full CRUD with date/category filters, newest-first sort
- **Savings Goals**: Goal tracking with dynamic progress computation
- **Reports**: Monthly and yearly aggregated income/expense reports by category
- **Security**: Complete user data isolation — no cross-user data access possible
- **Exception Handling**: Consistent JSON error responses for 400/401/403/404/409/500

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.5 |
| Language | Java 17 |
| Security | Spring Security (session-based) |
| ORM | Spring Data JPA + Hibernate |
| Database (dev) | H2 In-Memory |
| Database (prod) | PostgreSQL |
| Build | Maven |
| Utilities | Lombok |
| Tests | JUnit 5 + Mockito |

---

## Project Structure

```
src/main/java/com/adarsh/financemanager/
├── config/             # DataSeeder (CommandLineRunner)
├── controller/         # REST controllers
├── dto/                # Request/Response DTOs
├── entity/             # JPA entities + enums
├── exception/          # Custom exceptions + GlobalExceptionHandler
├── repository/         # Spring Data JPA repositories
├── security/           # SecurityConfig, SecurityUtils
└── service/            # Service interfaces + implementations
```

---

## Setup & Running (Dev)

### Prerequisites
- Java 17+
- Maven (or use the included `mvnw`)

### Steps

```bash
# Clone the project
git clone <repo-url>
cd personal-finance-manager/backend

# Run the application
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**

### H2 Console
Access the in-memory database at: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`
- Password: *(leave blank)*

---

## Authentication Flow

All protected endpoints require an active session cookie. Here's the flow:

```
1. POST /api/auth/register  → create account
2. POST /api/auth/login     → get JSESSIONID cookie
3. Use cookie in all subsequent requests
4. POST /api/auth/logout    → invalidate session
```

Postman: After login, the `JSESSIONID` cookie is automatically sent with subsequent requests.

---

## API Endpoints

### Auth
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login | No |
| POST | `/api/auth/logout` | Logout | No |

### Categories
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/categories` | List all (default + own custom) | Yes |
| POST | `/api/categories` | Create custom category | Yes |
| DELETE | `/api/categories/{name}` | Delete custom category | Yes |

### Transactions
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/transactions` | Create transaction | Yes |
| GET | `/api/transactions` | List transactions (with filters) | Yes |
| PUT | `/api/transactions/{id}` | Update transaction | Yes |
| DELETE | `/api/transactions/{id}` | Delete transaction | Yes |

**GET Filters (query params):**
- `startDate` — ISO format `YYYY-MM-DD`
- `endDate` — ISO format `YYYY-MM-DD`
- `categoryId` — numeric category ID

### Savings Goals
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/goals` | Create goal | Yes |
| GET | `/api/goals` | List all goals with progress | Yes |
| GET | `/api/goals/{id}` | Get specific goal with progress | Yes |
| PUT | `/api/goals/{id}` | Update goal | Yes |
| DELETE | `/api/goals/{id}` | Delete goal | Yes |

### Reports
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/reports/monthly/{year}/{month}` | Monthly income/expense report | Yes |
| GET | `/api/reports/yearly/{year}` | Yearly income/expense report | Yes |

---

## Request/Response Examples

### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securepassword",
  "fullName": "Adarsh Kumar",
  "phoneNumber": "9876543210"
}
```

### Create Transaction
```http
POST /api/transactions
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "amount": 5000.00,
  "date": "2025-05-20",
  "description": "Monthly salary",
  "categoryId": 1
}
```

### Get Monthly Report
```http
GET /api/reports/monthly/2025/5
Cookie: JSESSIONID=...
```

Response:
```json
{
  "year": 2025,
  "month": 5,
  "incomeByCategory": [
    { "categoryName": "Salary", "categoryType": "INCOME", "totalAmount": 50000.00 }
  ],
  "expensesByCategory": [
    { "categoryName": "Food", "categoryType": "EXPENSE", "totalAmount": 8000.00 }
  ],
  "totalIncome": 50000.00,
  "totalExpenses": 8000.00,
  "netSavings": 42000.00
}
```

---

## Error Responses

All errors return a consistent JSON format:

```json
{ "error": "Error message here" }
```

| Status | Scenario |
|--------|----------|
| 400 | Validation failure, category in use |
| 401 | Invalid credentials |
| 403 | Attempting to delete default category |
| 404 | Resource not found |
| 409 | Duplicate username / category |
| 500 | Unexpected server error |

---

## Default Categories (Auto-seeded)

| Name | Type |
|------|------|
| Salary | INCOME |
| Food | EXPENSE |
| Rent | EXPENSE |
| Transportation | EXPENSE |
| Entertainment | EXPENSE |
| Healthcare | EXPENSE |
| Utilities | EXPENSE |

---

## Running Tests

```bash
./mvnw test
```

Test coverage targets 80%+ on service layer (Mockito unit tests).

---

## Deployment to Render

### Steps
1. Push the project to GitHub
2. Create a new **Web Service** on [Render](https://render.com)
3. Set **Build Command**: `./mvnw clean package -DskipTests`
4. Set **Start Command**: `java -jar target/finance-manager-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
5. Set the following **Environment Variables** in Render dashboard:
   - `DATABASE_URL` → your PostgreSQL JDBC URL
   - `DB_USERNAME` → database username
   - `DB_PASSWORD` → database password
6. Add a **PostgreSQL** database on Render and copy the connection details

The app will start using the `application-prod.properties` profile automatically.

---

## Architecture

```
Client (Postman / Frontend)
        │
        ▼
┌───────────────────────┐
│   Spring Security     │  Session validation, CSRF disabled
└───────────┬───────────┘
            │
┌───────────▼───────────┐
│     Controllers       │  HTTP layer — no business logic
└───────────┬───────────┘
            │
┌───────────▼───────────┐
│      Services         │  Business logic, ownership checks
└───────────┬───────────┘
            │
┌───────────▼───────────┐
│    Repositories       │  Spring Data JPA + custom JPQL
└───────────┬───────────┘
            │
┌───────────▼───────────┐
│   H2 / PostgreSQL     │  Persistent storage
└───────────────────────┘
```
