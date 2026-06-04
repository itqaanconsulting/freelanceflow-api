package nl.itqaanconsulting.freelanceflow.demo;

import java.util.UUID;

record DemoResetResponse(
        UUID customerId,
        UUID projectId,
        UUID invoiceId,
        int timeEntries,
        int auditEvents
) {
}
