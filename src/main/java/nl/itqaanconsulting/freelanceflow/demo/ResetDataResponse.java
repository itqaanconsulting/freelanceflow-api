package nl.itqaanconsulting.freelanceflow.demo;

import java.util.UUID;

record ResetDataResponse(
        UUID customerId,
        UUID projectId,
        UUID invoiceId,
        int timeEntries,
        int auditEvents
) {
}
