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
- OpenAPI/Swagger API documentation
- Testable modular package structure

## Current scope

Version `0.1` starts with customer management as the foundation for the invoice workflow.

Planned workflow:

```text
Customer -> Project -> Time Entry -> Approval -> Invoice -> Payment Status
```

Current features:

- Customer CRUD API
- Project CRUD API linked to customers
- Project status model: active, paused, completed
- Time entry CRUD API linked to projects
- Time entry workflow: draft, submitted, approved, rejected
- Request validation
- Duplicate customer email checks
- Duplicate project name checks per customer
- Consistent API error responses
- PostgreSQL schema migration with Flyway
- Swagger UI

## Tech stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway
- Docker Compose
- Maven
- JUnit 5
- Testcontainers

## Run locally

Start PostgreSQL:

```bash
docker compose up -d
```

Run the API:

```bash
mvn spring-boot:run
```

Open Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

## Example request

Create a customer:

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

Create a project:

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

Create and submit a time entry:

```bash
curl -X POST http://localhost:8080/api/time-entries \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "<project-id>",
    "workDate": "2026-06-03",
    "hours": 7.50,
    "description": "Implemented customer and project API workflow"
  }'

curl -X POST http://localhost:8080/api/time-entries/<time-entry-id>/submit
curl -X POST http://localhost:8080/api/time-entries/<time-entry-id>/approve
```

## Roadmap

- Invoice generation from approved time entries
- Audit logging for workflow changes
- OAuth2/JWT security with Keycloak
- Integration events for invoice lifecycle changes
- GitHub Actions CI pipeline
