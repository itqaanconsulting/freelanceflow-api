package nl.itqaanconsulting.freelanceflow.timeentry;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TimeEntryRequest(
        @NotNull UUID projectId,
        @NotNull LocalDate workDate,
        @NotNull @DecimalMin("0.25") @DecimalMax("24.00") BigDecimal hours,
        @NotBlank @Size(max = 1000) String description
) {
}
