# FreelanceFlow API

FreelanceFlow API is a Spring Boot application for freelance time tracking, approval workflows and invoice generation.

The project models a realistic administration flow with secured REST APIs, PostgreSQL persistence, Flyway migrations, Keycloak-based JWT authorization, audit logging, PDF invoice export and automated tests.

## Project Scope

The codebase focuses on backend engineering topics that are common in enterprise and consultancy environments:

- Java 21 and Spring Boot backend development
- REST API design with validation and consistent errors
- Workflow-oriented domain modelling
- PostgreSQL persistence with JPA/Hibernate
- Flyway database migrations
- OAuth2/JWT security with Keycloak
- Role-based authorization
- Audit logging for business events, including the acting user
- Unique, gap-free invoice numbers per year, safe under concurrent requests
- PDF generation
- Docker-based local development
- CI with Maven and GitHub Actions
- Integration testing with Testcontainers

## Workflow

```text
Customer -> Project -> Time Entry -> Approval -> Invoice -> Payment -> Audit
```

The application supports a complete freelance administration flow:

- Register customers.
- Create billable projects.
- Log work as time entries.
- Submit and approve time entries.
- Generate invoices from approved, uninvoiced work.
- Download invoices as PDF.
- Mark invoices as paid.
- Inspect audit events for workflow changes.

## Web UI

A static web UI is served by Spring Boot:

```text
http://localhost:8080/demo/
```

Recommended local flow:

1. Login with `admin / admin`.
2. Click `Reset base data`.
3. Load the dashboard.
4. Review the customer, project, time entries and invoice.
5. Download the invoice PDF.
6. Load audit events.
7. Optionally create extra data through `Manual entry`.

The reset creates a stable base dataset:

- 1 customer: `Acme Consulting`
- 1 project: `Backend Modernization`
- 3 time entries: `DRAFT`, `SUBMITTED`, `APPROVED`
- 1 issued invoice
- 7 audit events

## API Capabilities

- Customer CRUD API
- Project CRUD API linked to customers
- Time entry CRUD API linked to projects
- Time entry workflow: `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED`
- Invoice generation from approved time entries
- Invoice PDF export
- Invoice workflow: `ISSUED`, `PAID`, `CANCELLED`
- Audit logging for time entry and invoice events
- Admin-only data reset for local presentation
- Swagger/OpenAPI documentation
- Consistent validation and error responses

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security OAuth2 Resource Server
- Bean Validation
- PostgreSQL
- Keycloak
- Flyway
- OpenPDF
- Docker Compose
- Maven
- JUnit 5
- Testcontainers
- GitHub Actions

## Run Locally

Start PostgreSQL and Keycloak:

```bash
docker compose up -d
```

PostgreSQL is exposed on `localhost:5433`.
Keycloak is exposed on `http://localhost:8180`.

Run the API:

```bash
mvn spring-boot:run
```

Open the web UI:

```text
http://localhost:8080/demo/
```

Open Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

## Demo Users

| Username | Password | Role |
| --- | --- | --- |
| `freelancer` | `freelancer` | `FREELANCER` |
| `accountant` | `accountant` | `ACCOUNTANT` |
| `admin` | `admin` | `ADMIN` |

## Security Rules

- Demo UI, Swagger UI, OpenAPI docs and health/info actuator endpoints are public.
- Customer, project, time-entry and invoice generation endpoints require `FREELANCER` or `ADMIN`.
- Approving or rejecting time entries requires `ACCOUNTANT` or `ADMIN`: a freelancer cannot approve their own hours (segregation of duties). Accountants can also read time entries to review them.
- Invoice payment endpoint requires `ACCOUNTANT` or `ADMIN`.
- Audit events require `ADMIN`.
- Demo reset requires `ADMIN`.
- JWT roles are read from Keycloak `realm_access.roles`.

## Main Endpoints

| Capability | Endpoint |
| --- | --- |
| Customers | `GET /api/customers`, `POST /api/customers` |
| Projects | `GET /api/projects`, `POST /api/projects` |
| Time entries | `GET /api/time-entries`, `POST /api/time-entries` |
| Submit time entry | `POST /api/time-entries/{id}/submit` |
| Approve time entry | `POST /api/time-entries/{id}/approve` |
| Generate invoice | `POST /api/invoices/generate` |
| Mark invoice paid | `POST /api/invoices/{id}/mark-paid` |
| Download invoice PDF | `GET /api/invoices/{id}/pdf` |
| Audit events | `GET /api/audit-events` |
| Data reset | `POST /api/demo/reset` |

## Workflow Rules

- Only `DRAFT` time entries can be updated or deleted.
- Only `DRAFT` time entries can be submitted.
- Only `SUBMITTED` time entries can be approved or rejected.
- Only `APPROVED` time entries can be invoiced.
- A time entry can only be invoiced once.
- Invoice generation fails when a project has no approved, uninvoiced time entries.
- Only `ISSUED` invoices can be marked as paid or cancelled.

## Invoice Numbering

Invoice numbers follow `INV-<year>-<sequence>`. The last sequence per year is kept in `invoice_number_counters`; its row is locked with `SELECT ... FOR UPDATE` while a number is drawn and stays locked until the invoice is committed. Concurrent requests therefore never receive the same number, and a rolled-back invoice does not leave a gap.

## Audit Events

Recorded events include:

- `TIME_ENTRY_CREATED`
- `TIME_ENTRY_SUBMITTED`
- `TIME_ENTRY_APPROVED`
- `TIME_ENTRY_REJECTED`
- `INVOICE_GENERATED`
- `INVOICE_PAID`
- `INVOICE_CANCELLED`

Every audit event records the acting user (`actor`), taken from the Keycloak `preferred_username` claim. Events created outside a user request are recorded as `system`.

Audit events can be queried globally or by aggregate:

```text
GET /api/audit-events
GET /api/audit-events?aggregateType=TIME_ENTRY&aggregateId=<time-entry-id>
GET /api/audit-events?aggregateType=INVOICE&aggregateId=<invoice-id>
```

## Testing

Run the default test suite:

```bash
mvn test
```

Run PostgreSQL integration tests with Testcontainers:

```bash
mvn verify -Ppostgres-it
```

GitHub Actions runs:

```bash
mvn test
mvn verify -Ppostgres-it
```

## Notes

This is not a production SaaS. The reset endpoint intentionally recreates local data and is restricted to `ADMIN`.
