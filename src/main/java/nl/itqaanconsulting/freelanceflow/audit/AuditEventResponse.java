package nl.itqaanconsulting.freelanceflow.audit;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        String aggregateType,
        UUID aggregateId,
        String eventType,
        String message,
        OffsetDateTime createdAt
) {
    static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                event.getMessage(),
                event.getCreatedAt()
        );
    }
}
