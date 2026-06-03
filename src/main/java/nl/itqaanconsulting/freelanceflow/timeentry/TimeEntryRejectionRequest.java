package nl.itqaanconsulting.freelanceflow.timeentry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TimeEntryRejectionRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
