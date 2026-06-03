package nl.itqaanconsulting.freelanceflow.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID projectId,
        String projectName,
        UUID customerId,
        String customerName,
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        InvoiceStatus status,
        String currency,
        BigDecimal totalAmount,
        List<InvoiceLineResponse> lines,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getProject().getId(),
                invoice.getProject().getName(),
                invoice.getProject().getCustomer().getId(),
                invoice.getProject().getCustomer().getCompanyName(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getCurrency(),
                invoice.getTotalAmount(),
                invoice.getLines().stream().map(InvoiceLineResponse::from).toList(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
