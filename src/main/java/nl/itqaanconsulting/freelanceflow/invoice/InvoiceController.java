package nl.itqaanconsulting.freelanceflow.invoice;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    InvoiceController(InvoiceService invoiceService, InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping
    List<InvoiceResponse> findAll(@RequestParam(required = false) UUID projectId) {
        return invoiceService.findAll(projectId);
    }

    @GetMapping("/{id}")
    InvoiceResponse findById(@PathVariable UUID id) {
        return invoiceService.findById(id);
    }

    @GetMapping("/{id}/pdf")
    ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        InvoicePdf pdf = invoicePdfService.generate(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", ContentDisposition.attachment()
                        .filename(pdf.filename())
                        .build()
                        .toString())
                .body(pdf.content());
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    InvoiceResponse generate(@Valid @RequestBody InvoiceGenerationRequest request) {
        return invoiceService.generate(request);
    }

    @PostMapping("/{id}/mark-paid")
    InvoiceResponse markPaid(@PathVariable UUID id) {
        return invoiceService.markPaid(id);
    }

    @PostMapping("/{id}/cancel")
    InvoiceResponse cancel(@PathVariable UUID id) {
        return invoiceService.cancel(id);
    }
}
