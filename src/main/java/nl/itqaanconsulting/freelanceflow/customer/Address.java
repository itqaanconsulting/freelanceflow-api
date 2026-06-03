package nl.itqaanconsulting.freelanceflow.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        @Column(length = 160) String street,
        @Column(length = 120) String city,
        @Column(length = 80) String country
) {
}
