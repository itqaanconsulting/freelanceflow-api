package nl.itqaanconsulting.freelanceflow.invoice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    List<InvoiceResponse> findAll(@RequestParam(required = false) UUID projectId) {
        return invoiceService.findAll(projectId);
    }

    @GetMapping("/{id}")
    InvoiceResponse findById(@PathVariable UUID id) {
        return invoiceService.findById(id);
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
