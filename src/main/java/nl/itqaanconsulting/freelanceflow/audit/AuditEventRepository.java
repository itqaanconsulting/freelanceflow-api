package nl.itqaanconsulting.freelanceflow.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    List<AuditEvent> findAllByOrderByCreatedAtDesc();

    List<AuditEvent> findAllByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(String aggregateType, UUID aggregateId);
}
