# FreelanceFlow API

FreelanceFlow API is a Spring Boot backend showcase project for freelance time tracking and invoice workflows.

The project is designed to demonstrate secure REST API development, workflow-oriented domain modelling, database migrations, validation, testability and integration-ready architecture for freelance and consultancy businesses.

## Why this project exists

This repository is part of Hani El Amam's freelance Java portfolio. It focuses on practical backend engineering skills that are relevant in enterprise and consultancy environments:

- Java 21 and Spring Boot backend development
- REST API design with validation and consistent error responses
- PostgreSQL persistence with JPA/Hibernate
- Flyway database migrations
- Docker-based local development
- OAuth2/JWT API security with Keycloak
- OpenAPI/Swagger API documentation
- Testable modular package structure
- PostgreSQL integration testing with Testcontainers

## What this project demonstrates

FreelanceFlow models a realistic freelance administration workflow:

- A customer is registered.
- A billable project is created for that customer.
- Work is logged as time entries.
- Time entries move through an approval workflow.
- Approved work is converted into an invoice.
- The invoice can be marked as paid or cancelled.

The main workflow is:

```text
Customer -> Project -> Time Entry -> Approval -> Invoice -> Payment
```

Current capabilities:

- Customer CRUD API
- Project CRUD API linked to customers
- Project status model: active, paused, completed
- Time entry CRUD API linked to projects
- Time entry workflow: draft, submitted, approved, rejected
- Invoice generation from approved time entries
- PDF export for generated invoices
- Invoice status model: issued, paid, cancelled
- Audit logging for time entry and invoice workflow changes
- OAuth2/JWT security with role-based authorization
- Request validation
- Duplicate customer email checks
- Duplicate project name checks per customer
- Consistent API error responses
- PostgreSQL schema migration with Flyway
- Swagger UI
- Maven CI pipeline with GitHub Actions

## Tech stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Keycloak
- Spring Security OAuth2 Resource Server
- Flyway
- Docker Compose
- Maven
- JUnit 5
- Testcontainers

## Run locally

Start PostgreSQL and Keycloak:

```bash
docker compose up -d
```

The local PostgreSQL container is exposed on host port `5433` to avoid conflicts with existing PostgreSQL installations.
Keycloak is exposed on `http://localhost:8180`.

Run the API:

```bash
mvn spring-boot:run
```

Open Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Keycloak admin console:

```text
http://localhost:8180
```

Admin credentials:

```text
admin / admin
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

## Demo scenario

The fastest way to explore the API is through Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Click `Authorize` in Swagger and log in with one of the demo users.

Demo users:

| Username | Password | Role |
| --- | --- | --- |
| `freelancer` | `freelancer` | `FREELANCER` |
| `accountant` | `accountant` | `ACCOUNTANT` |
| `admin` | `admin` | `ADMIN` |

Use this scenario to demo the complete workflow.

### 1. Create a customer

Endpoint:

```text
POST /api/customers
```

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Acme Consulting",
    "contactName": "Jane Doe",
    "email": "jane@example.com",
    "phone": "+31 20 123 4567",
    "vatNumber": "NL123456789B01",
    "street": "Keizersgracht 1",
    "city": "Amsterdam",
    "country": "Netherlands"
  }'
```

Copy the returned `id`. This is the `customerId` for the next step.

### 2. Create a project

Endpoint:

```text
POST /api/projects
```

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "<customer-id>",
    "name": "Backend modernization",
    "description": "Spring Boot API modernization and integration improvements",
    "hourlyRate": 95.00,
    "currency": "EUR",
    "status": "ACTIVE",
    "startDate": "2026-06-01"
  }'
```

Copy the returned `id`. This is the `projectId` for the next step.

### 3. Create a time entry

Endpoint:

```text
POST /api/time-entries
```

```bash
curl -X POST http://localhost:8080/api/time-entries \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "<project-id>",
    "workDate": "2026-06-03",
    "hours": 7.50,
    "description": "Implemented customer and project API workflow"
  }'
```

The time entry starts with status `DRAFT`. Copy the returned `id`.

### 4. Submit and approve the time entry

Endpoints:

```text
POST /api/time-entries/{id}/submit
POST /api/time-entries/{id}/approve
```

```bash
curl -X POST http://localhost:8080/api/time-entries/<time-entry-id>/submit
curl -X POST http://localhost:8080/api/time-entries/<time-entry-id>/approve
```

After approval, the time entry has status `APPROVED` and can be invoiced.

### 5. Generate an invoice

Endpoint:

```text
POST /api/invoices/generate
```

```bash
curl -X POST http://localhost:8080/api/invoices/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "<project-id>",
    "issueDate": "2026-06-03",
    "dueDate": "2026-06-17"
  }'
```

The invoice contains one line per approved, uninvoiced time entry. Copy the returned `id`.

### 6. Mark the invoice as paid

Endpoint:

```text
POST /api/invoices/{id}/mark-paid
```

```bash
curl -X POST http://localhost:8080/api/invoices/<invoice-id>/mark-paid
```

The invoice now has status `PAID`.

### 7. Download the invoice PDF

Endpoint:

```text
GET /api/invoices/{id}/pdf
```

```bash
curl -L http://localhost:8080/api/invoices/<invoice-id>/pdf \
  -H "Authorization: Bearer <access-token>" \
  -o invoice.pdf
```

The response is an `application/pdf` document containing the invoice number, customer, project, line items and total.

## API workflow rules

- Only `DRAFT` time entries can be updated or deleted.
- Only `DRAFT` time entries can be submitted.
- Only `SUBMITTED` time entries can be approved or rejected.
- Only `APPROVED` time entries can be invoiced.
- A time entry can only be invoiced once.
- Invoice generation fails when a project has no approved, uninvoiced time entries.
- Only `ISSUED` invoices can be marked as paid or cancelled.
- Invoice PDFs can be downloaded after invoice generation.

## Security rules

- Swagger UI, OpenAPI docs and health/info actuator endpoints are public.
- Customer, project, time-entry and invoice generation endpoints require `FREELANCER` or `ADMIN`.
- Invoice payment endpoint requires `ACCOUNTANT` or `ADMIN`.
- Audit events require `ADMIN`.
- JWT roles are read from Keycloak `realm_access.roles`.

## Audit logging

Workflow changes are recorded as audit events and can be inspected through:

```text
GET /api/audit-events
GET /api/audit-events?aggregateType=TIME_ENTRY&aggregateId=<time-entry-id>
GET /api/audit-events?aggregateType=INVOICE&aggregateId=<invoice-id>
```

Recorded events include:

- `TIME_ENTRY_CREATED`
- `TIME_ENTRY_SUBMITTED`
- `TIME_ENTRY_APPROVED`
- `TIME_ENTRY_REJECTED`
- `INVOICE_GENERATED`
- `INVOICE_PAID`
- `INVOICE_CANCELLED`

## Continuous integration

GitHub Actions runs two test jobs on pushes and pull requests to `main`:

```bash
mvn test
mvn verify -Ppostgres-it
```

The default test suite uses an in-memory test database for fast feedback. The `postgres-it` profile runs a full workflow against a real PostgreSQL container with Testcontainers.

To run the PostgreSQL integration test locally, make sure Docker is running and execute:

```bash
mvn verify -Ppostgres-it
```

## Roadmap

- Integration events for invoice lifecycle changes
- Frontend demo application
