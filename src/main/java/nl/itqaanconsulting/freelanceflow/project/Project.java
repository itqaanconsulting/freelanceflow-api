package nl.itqaanconsulting.freelanceflow.project;

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
import nl.itqaanconsulting.freelanceflow.customer.Customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected Project() {
    }

    public Project(Customer customer, String name, String description, BigDecimal hourlyRate, String currency,
                   ProjectStatus status, LocalDate startDate, LocalDate endDate) {
        this.id = UUID.randomUUID();
        this.customer = customer;
        this.name = name;
        this.description = description;
        this.hourlyRate = hourlyRate;
        this.currency = currency;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
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

    void update(ProjectRequest request) {
        this.name = request.name();
        this.description = request.description();
        this.hourlyRate = request.hourlyRate();
        this.currency = request.currency();
        this.status = request.status();
        this.startDate = request.startDate();
        this.endDate = request.endDate();
    }

    public UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public String getCurrency() {
        return currency;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
