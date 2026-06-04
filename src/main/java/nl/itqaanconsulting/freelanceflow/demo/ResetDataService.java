package nl.itqaanconsulting.freelanceflow.demo;

import nl.itqaanconsulting.freelanceflow.audit.AuditService;
import nl.itqaanconsulting.freelanceflow.customer.Address;
import nl.itqaanconsulting.freelanceflow.customer.Customer;
import nl.itqaanconsulting.freelanceflow.customer.CustomerRepository;
import nl.itqaanconsulting.freelanceflow.invoice.Invoice;
import nl.itqaanconsulting.freelanceflow.invoice.InvoiceLine;
import nl.itqaanconsulting.freelanceflow.invoice.InvoiceRepository;
import nl.itqaanconsulting.freelanceflow.project.Project;
import nl.itqaanconsulting.freelanceflow.project.ProjectRepository;
import nl.itqaanconsulting.freelanceflow.project.ProjectStatus;
import nl.itqaanconsulting.freelanceflow.timeentry.TimeEntry;
import nl.itqaanconsulting.freelanceflow.timeentry.TimeEntryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
class ResetDataService {

    private final JdbcTemplate jdbcTemplate;
    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditService auditService;

    ResetDataService(
            JdbcTemplate jdbcTemplate,
            CustomerRepository customerRepository,
            ProjectRepository projectRepository,
            TimeEntryRepository timeEntryRepository,
            InvoiceRepository invoiceRepository,
            AuditService auditService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.customerRepository = customerRepository;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.invoiceRepository = invoiceRepository;
        this.auditService = auditService;
    }

    @Transactional
    ResetDataResponse reset() {
        clearData();

        LocalDate today = LocalDate.now();
        Customer customer = customerRepository.save(new Customer(
                "Acme Consulting",
                "Jane Doe",
                "jane.doe@acme.example",
                "+31 20 123 4567",
                "NL123456789B01",
                new Address("Keizersgracht 1", "Amsterdam", "Netherlands")
        ));

        Project project = projectRepository.save(new Project(
                customer,
                "Backend Modernization",
                "Spring Boot API modernization, security and invoice workflow.",
                new BigDecimal("95.00"),
                "EUR",
                ProjectStatus.ACTIVE,
                today.minusDays(14),
                null
        ));

        TimeEntry draft = saveTimeEntry(project, today.minusDays(3), "Prepared API design and validation rules.", "TIME_ENTRY_CREATED");

        TimeEntry submitted = saveTimeEntry(project, today.minusDays(2), "Implemented secured time tracking workflow.", "TIME_ENTRY_CREATED");
        submitted.submit();
        auditService.record("TIME_ENTRY", submitted.getId(), "TIME_ENTRY_SUBMITTED", "Time entry submitted for approval");

        TimeEntry approved = saveTimeEntry(project, today.minusDays(1), "Implemented invoice generation and PDF export.", "TIME_ENTRY_CREATED");
        approved.submit();
        auditService.record("TIME_ENTRY", approved.getId(), "TIME_ENTRY_SUBMITTED", "Time entry submitted for approval");
        approved.approve();
        auditService.record("TIME_ENTRY", approved.getId(), "TIME_ENTRY_APPROVED", "Time entry approved");

        Invoice invoice = invoiceRepository.save(new Invoice(
                project,
                "INV-%d-0001".formatted(today.getYear()),
                today,
                today.plusDays(14)
        ));
        InvoiceLine line = new InvoiceLine(invoice, approved, project.getHourlyRate());
        invoice.addLine(line);
        approved.assignInvoice(invoice);
        auditService.record(
                "INVOICE",
                invoice.getId(),
                "INVOICE_GENERATED",
                "Invoice %s generated for project %s with 1 line"
                        .formatted(invoice.getInvoiceNumber(), project.getId())
        );

        return new ResetDataResponse(customer.getId(), project.getId(), invoice.getId(), 3, 7);
    }

    private TimeEntry saveTimeEntry(Project project, LocalDate workDate, String description, String eventType) {
        TimeEntry timeEntry = timeEntryRepository.save(new TimeEntry(
                project,
                workDate,
                new BigDecimal("6.50"),
                description
        ));
        auditService.record("TIME_ENTRY", timeEntry.getId(), eventType, "Time entry created for project %s".formatted(project.getId()));
        return timeEntry;
    }

    private void clearData() {
        jdbcTemplate.update("DELETE FROM invoice_lines");
        jdbcTemplate.update("UPDATE time_entries SET invoice_id = NULL");
        jdbcTemplate.update("DELETE FROM audit_events");
        jdbcTemplate.update("DELETE FROM invoices");
        jdbcTemplate.update("DELETE FROM time_entries");
        jdbcTemplate.update("DELETE FROM projects");
        jdbcTemplate.update("DELETE FROM customers");
    }
}
