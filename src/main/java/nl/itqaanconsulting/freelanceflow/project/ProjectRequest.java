package nl.itqaanconsulting.freelanceflow.project;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectRequest(
        @NotNull UUID customerId,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 1000) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal hourlyRate,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @NotNull ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
}
