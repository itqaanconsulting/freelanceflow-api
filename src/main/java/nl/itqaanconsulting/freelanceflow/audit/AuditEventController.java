package nl.itqaanconsulting.freelanceflow.audit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-events")
class AuditEventController {

    private final AuditService auditService;

    AuditEventController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    List<AuditEventResponse> findAll(
            @RequestParam(required = false) String aggregateType,
            @RequestParam(required = false) UUID aggregateId
    ) {
        return auditService.findAll(aggregateType, aggregateId);
    }
}
