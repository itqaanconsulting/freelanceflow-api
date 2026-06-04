package nl.itqaanconsulting.freelanceflow.timeentry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import nl.itqaanconsulting.freelanceflow.invoice.Invoice;
import nl.itqaanconsulting.freelanceflow.project.Project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_entries")
public class TimeEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TimeEntryStatus status;

    @Column(length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected TimeEntry() {
    }

    public TimeEntry(Project project, LocalDate workDate, BigDecimal hours, String description) {
        this.id = UUID.randomUUID();
        this.project = project;
        this.workDate = workDate;
        this.hours = hours;
        this.description = description;
        this.status = TimeEntryStatus.DRAFT;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    void update(TimeEntryRequest request) {
        requireStatus(TimeEntryStatus.DRAFT, "Only draft time entries can be updated");
        this.workDate = request.workDate();
        this.hours = request.hours();
        this.description = request.description();
    }

    public void submit() {
        requireStatus(TimeEntryStatus.DRAFT, "Only draft time entries can be submitted");
        this.status = TimeEntryStatus.SUBMITTED;
        this.rejectionReason = null;
    }

    public void approve() {
        requireStatus(TimeEntryStatus.SUBMITTED, "Only submitted time entries can be approved");
        this.status = TimeEntryStatus.APPROVED;
        this.rejectionReason = null;
    }

    void reject(String reason) {
        requireStatus(TimeEntryStatus.SUBMITTED, "Only submitted time entries can be rejected");
        this.status = TimeEntryStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void assignInvoice(Invoice invoice) {
        requireStatus(TimeEntryStatus.APPROVED, "Only approved time entries can be invoiced");
        if (this.invoice != null) {
            throw new IllegalArgumentException("Time entry has already been invoiced");
        }
        this.invoice = invoice;
    }

    private void requireStatus(TimeEntryStatus expectedStatus, String message) {
        if (status != expectedStatus) {
            throw new IllegalArgumentException(message);
        }
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public String getDescription() {
        return description;
    }

    public TimeEntryStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
