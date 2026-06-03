package nl.itqaanconsulting.freelanceflow.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 160) String companyName,
        @Size(max = 120) String contactName,
        @NotBlank @Email @Size(max = 160) String email,
        @Size(max = 40) String phone,
        @Size(max = 40) String vatNumber,
        @Size(max = 160) String street,
        @Size(max = 120) String city,
        @Size(max = 80) String country
) {
}
