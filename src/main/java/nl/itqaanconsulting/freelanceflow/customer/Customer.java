package nl.itqaanconsulting.freelanceflow.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String companyName;

    @Column(length = 120)
    private String contactName;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 40)
    private String vatNumber;

    @Embedded
    private Address address;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected Customer() {
    }

    public Customer(String companyName, String contactName, String email, String phone, String vatNumber, Address address) {
        this.id = UUID.randomUUID();
        this.companyName = companyName;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.vatNumber = vatNumber;
        this.address = address;
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

    public void update(CustomerRequest request) {
        this.companyName = request.companyName();
        this.contactName = request.contactName();
        this.email = request.email();
        this.phone = request.phone();
        this.vatNumber = request.vatNumber();
        this.address = new Address(request.street(), request.city(), request.country());
    }

    public UUID getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public Address getAddress() {
        return address;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
