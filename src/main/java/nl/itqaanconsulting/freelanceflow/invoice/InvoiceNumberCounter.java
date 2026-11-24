package nl.itqaanconsulting.freelanceflow.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Last issued invoice sequence per calendar year. The row is locked while a new
 * number is drawn, so concurrent invoice generation cannot produce duplicates.
 */
@Entity
@Table(name = "invoice_number_counters")
class InvoiceNumberCounter {

    @Id
    @Column(name = "invoice_year")
    private int invoiceYear;

    @Column(name = "last_value", nullable = false)
    private long lastValue;

    protected InvoiceNumberCounter() {
    }

    InvoiceNumberCounter(int invoiceYear, long lastValue) {
        this.invoiceYear = invoiceYear;
        this.lastValue = lastValue;
    }

    long increment() {
        lastValue++;
        return lastValue;
    }
}
