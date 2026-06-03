package nl.itqaanconsulting.freelanceflow.timeentry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeEntryResponse(
        UUID id,
        UUID projectId,
        String projectName,
        UUID customerId,
        String customerName,
        LocalDate workDate,
        BigDecimal hours,
        String description,
        TimeEntryStatus status,
        String rejectionReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    static TimeEntryResponse from(TimeEntry timeEntry) {
        return new TimeEntryResponse(
                timeEntry.getId(),
                timeEntry.getProject().getId(),
                timeEntry.getProject().getName(),
                timeEntry.getProject().getCustomer().getId(),
                timeEntry.getProject().getCustomer().getCompanyName(),
                timeEntry.getWorkDate(),
                timeEntry.getHours(),
                timeEntry.getDescription(),
                timeEntry.getStatus(),
                timeEntry.getRejectionReason(),
                timeEntry.getCreatedAt(),
                timeEntry.getUpdatedAt()
        );
    }
}
