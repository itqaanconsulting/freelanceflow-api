package nl.itqaanconsulting.freelanceflow.audit;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
        auditEventRepository.save(new AuditEvent(aggregateType, aggregateId, eventType, message, currentActor()));
    }

    static final String SYSTEM_ACTOR = "system";

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return SYSTEM_ACTOR;
        }
        if (authentication instanceof JwtAuthenticationToken jwt) {
            String username = jwt.getToken().getClaimAsString("preferred_username");
            if (username != null && !username.isBlank()) {
                return username;
            }
        }
        return authentication.getName();
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
