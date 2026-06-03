package nl.itqaanconsulting.freelanceflow.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceLineResponse(
        UUID id,
        UUID timeEntryId,
        String description,
        LocalDate workDate,
        BigDecimal hours,
        BigDecimal hourlyRate,
        BigDecimal lineAmount
) {
    static InvoiceLineResponse from(InvoiceLine line) {
        return new InvoiceLineResponse(
                line.getId(),
                line.getTimeEntry().getId(),
                line.getDescription(),
                line.getWorkDate(),
                line.getHours(),
                line.getHourlyRate(),
                line.getLineAmount()
        );
    }
}
