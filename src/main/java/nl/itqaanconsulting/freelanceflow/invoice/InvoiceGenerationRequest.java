package nl.itqaanconsulting.freelanceflow.invoice;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record InvoiceGenerationRequest(
        @NotNull UUID projectId,
        @NotNull LocalDate issueDate,
        @NotNull LocalDate dueDate
) {
}
