package nl.itqaanconsulting.freelanceflow.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void record(String aggregateType, UUID aggregateId, String eventType, String message) {
        auditEventRepository.save(new AuditEvent(aggregateType, aggregateId, eventType, message));
    }

    @Transactional(readOnly = true)
    List<AuditEventResponse> findAll(String aggregateType, UUID aggregateId) {
        List<AuditEvent> events = aggregateType == null || aggregateId == null
                ? auditEventRepository.findAllByOrderByCreatedAtDesc()
                : auditEventRepository.findAllByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(aggregateType, aggregateId);

        return events.stream()
                .map(AuditEventResponse::from)
                .toList();
    }
}
