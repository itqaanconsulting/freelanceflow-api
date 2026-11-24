package nl.itqaanconsulting.freelanceflow.invoice;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Draws gap-free, unique invoice numbers per year (INV-2026-0001, INV-2026-0002, ...).
 * Runs inside the caller's transaction: the counter row stays locked until the
 * invoice is committed, so a rolled-back invoice does not consume a number.
 */
@Component
class InvoiceNumberGenerator {

    private final InvoiceNumberCounterRepository counterRepository;
    private final InvoiceRepository invoiceRepository;

    InvoiceNumberGenerator(InvoiceNumberCounterRepository counterRepository, InvoiceRepository invoiceRepository) {
        this.counterRepository = counterRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    String next(LocalDate issueDate) {
        int year = issueDate.getYear();
        InvoiceNumberCounter counter = counterRepository.findForUpdate(year)
                .orElseGet(() -> counterRepository.saveAndFlush(
                        new InvoiceNumberCounter(year, invoicesIssuedIn(year))
                ));
        return "INV-%d-%04d".formatted(year, counter.increment());
    }

    private long invoicesIssuedIn(int year) {
        return invoiceRepository.countByIssueDateBetween(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }
}
