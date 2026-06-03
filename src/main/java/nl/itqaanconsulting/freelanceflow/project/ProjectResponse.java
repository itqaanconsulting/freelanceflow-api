package nl.itqaanconsulting.freelanceflow.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID customerId,
        String customerName,
        String name,
        String description,
        BigDecimal hourlyRate,
        String currency,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getCustomer().getId(),
                project.getCustomer().getCompanyName(),
                project.getName(),
                project.getDescription(),
                project.getHourlyRate(),
                project.getCurrency(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
