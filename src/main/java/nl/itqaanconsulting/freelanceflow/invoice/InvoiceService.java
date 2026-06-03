package nl.itqaanconsulting.freelanceflow.invoice;

import nl.itqaanconsulting.freelanceflow.project.Project;
import nl.itqaanconsulting.freelanceflow.project.ProjectRepository;
import nl.itqaanconsulting.freelanceflow.shared.ResourceNotFoundException;
import nl.itqaanconsulting.freelanceflow.timeentry.TimeEntry;
import nl.itqaanconsulting.freelanceflow.timeentry.TimeEntryRepository;
import nl.itqaanconsulting.freelanceflow.timeentry.TimeEntryStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;

    InvoiceService(InvoiceRepository invoiceRepository, ProjectRepository projectRepository,
                   TimeEntryRepository timeEntryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    @Transactional(readOnly = true)
    List<InvoiceResponse> findAll(UUID projectId) {
        List<Invoice> invoices = projectId == null
                ? invoiceRepository.findAll()
                : invoiceRepository.findAllByProjectId(projectId);

        return invoices.stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    InvoiceResponse findById(UUID id) {
        return InvoiceResponse.from(findInvoice(id));
    }

    @Transactional
    InvoiceResponse generate(InvoiceGenerationRequest request) {
        if (request.dueDate().isBefore(request.issueDate())) {
            throw new IllegalArgumentException("Due date cannot be before issue date");
        }

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.projectId()));

        List<TimeEntry> approvedEntries = timeEntryRepository.findAllByProjectIdAndStatusAndInvoiceIsNull(
                project.getId(),
                TimeEntryStatus.APPROVED
        );

        if (approvedEntries.isEmpty()) {
            throw new IllegalArgumentException("No approved uninvoiced time entries found for project: " + project.getId());
        }

        Invoice invoice = invoiceRepository.save(new Invoice(
                project,
                nextInvoiceNumber(request.issueDate()),
                request.issueDate(),
                request.dueDate()
        ));
        approvedEntries.forEach(timeEntry -> {
            InvoiceLine line = new InvoiceLine(invoice, timeEntry, project.getHourlyRate());
            invoice.addLine(line);
            timeEntry.assignInvoice(invoice);
        });

        return InvoiceResponse.from(invoice);
    }

    @Transactional
    InvoiceResponse markPaid(UUID id) {
        Invoice invoice = findInvoice(id);
        invoice.markPaid();
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    InvoiceResponse cancel(UUID id) {
        Invoice invoice = findInvoice(id);
        invoice.cancel();
        return InvoiceResponse.from(invoice);
    }

    private Invoice findInvoice(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    private String nextInvoiceNumber(LocalDate issueDate) {
        LocalDate firstDayOfYear = LocalDate.of(issueDate.getYear(), 1, 1);
        LocalDate lastDayOfYear = LocalDate.of(issueDate.getYear(), 12, 31);
        long nextSequence = invoiceRepository.countByIssueDateBetween(firstDayOfYear, lastDayOfYear) + 1;
        return "INV-%d-%04d".formatted(issueDate.getYear(), nextSequence);
    }
}
