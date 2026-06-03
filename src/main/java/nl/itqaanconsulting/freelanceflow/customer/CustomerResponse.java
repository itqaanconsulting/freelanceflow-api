package nl.itqaanconsulting.freelanceflow.customer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String companyName,
        String contactName,
        String email,
        String phone,
        String vatNumber,
        String street,
        String city,
        String country,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    static CustomerResponse from(Customer customer) {
        Address address = customer.getAddress();
        return new CustomerResponse(
                customer.getId(),
                customer.getCompanyName(),
                customer.getContactName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getVatNumber(),
                address == null ? null : address.street(),
                address == null ? null : address.city(),
                address == null ? null : address.country(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
