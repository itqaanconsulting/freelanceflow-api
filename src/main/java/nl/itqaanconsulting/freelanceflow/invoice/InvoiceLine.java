package nl.itqaanconsulting.freelanceflow.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import nl.itqaanconsulting.freelanceflow.timeentry.TimeEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoice_lines")
public class InvoiceLine {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_entry_id", nullable = false)
    private TimeEntry timeEntry;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineAmount;

    protected InvoiceLine() {
    }

    public InvoiceLine(Invoice invoice, TimeEntry timeEntry, BigDecimal hourlyRate) {
        this.id = UUID.randomUUID();
        this.invoice = invoice;
        this.timeEntry = timeEntry;
        this.description = timeEntry.getDescription();
        this.workDate = timeEntry.getWorkDate();
        this.hours = timeEntry.getHours();
        this.hourlyRate = hourlyRate;
        this.lineAmount = timeEntry.getHours().multiply(hourlyRate);
    }

    public UUID getId() {
        return id;
    }

    public TimeEntry getTimeEntry() {
        return timeEntry;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public BigDecimal getLineAmount() {
        return lineAmount;
    }
}
