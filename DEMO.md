# FreelanceFlow Demo Walkthrough

Use this guide to present the project quickly and consistently.

## 1. Start Dependencies

```bash
docker compose up -d
```

This starts:

- PostgreSQL on `localhost:5433`
- Keycloak on `http://localhost:8180`

## 2. Start The API

```bash
mvn spring-boot:run
```

Check health:

```text
http://localhost:8080/actuator/health
```

## 3. Open The Demo UI

```text
http://localhost:8080/demo/
```

Login:

```text
admin / admin
```

## 4. Reset Demo Data

Click:

```text
Reset demo data
```

Expected result:

- 1 customer
- 1 project
- 3 time entries
- 1 invoice
- 7 audit events

## 5. Explain The Workflow

The demo models this business process:

```text
Customer -> Project -> Time Entry -> Approval -> Invoice -> Audit
```

Show:

- Customer: `Acme Consulting`
- Project: `Backend Modernization`
- Time entries in different states
- Issued invoice
- Audit trail

## 6. Download Invoice PDF

In the invoice table, click:

```text
Download PDF
```

This calls:

```text
GET /api/invoices/{id}/pdf
```

Explain that the PDF is generated from the persisted invoice and invoice lines.

## 7. Show Audit Events

Click:

```text
Load audit events
```

Explain that audit events are admin-only and capture workflow changes such as:

- `TIME_ENTRY_CREATED`
- `TIME_ENTRY_SUBMITTED`
- `TIME_ENTRY_APPROVED`
- `INVOICE_GENERATED`

## 8. Optional Manual Flow

Open:

```text
Manual entry
```

Create an extra:

- customer
- project
- time entry

Then submit, approve and generate another invoice.

## 9. Swagger

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

Use it to inspect or call the REST API directly.

## 10. Final Verification

Run:

```bash
mvn test
```

Expected result:

```text
BUILD SUCCESS
```

