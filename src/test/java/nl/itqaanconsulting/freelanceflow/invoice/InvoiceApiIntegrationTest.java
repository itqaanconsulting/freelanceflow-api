package nl.itqaanconsulting.freelanceflow.invoice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
class InvoiceApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generatesInvoiceFromApprovedTimeEntriesAndMarksItPaid() throws Exception {
        UUID projectId = createProject();
        UUID timeEntryId = createApprovedTimeEntry(projectId);

        String response = mockMvc.perform(post("/api/invoices/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": "%s",
                                  "issueDate": "2026-06-03",
                                  "dueDate": "2026-06-17"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.invoiceNumber").value("INV-2026-0001"))
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.totalAmount").value(712.50))
                .andExpect(jsonPath("$.lines", hasSize(1)))
                .andExpect(jsonPath("$.lines[0].timeEntryId").value(timeEntryId.toString()))
                .andExpect(jsonPath("$.lines[0].hours").value(7.50))
                .andExpect(jsonPath("$.lines[0].hourlyRate").value(95.00))
                .andExpect(jsonPath("$.lines[0].lineAmount").value(712.50))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID invoiceId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        byte[] pdf = mockMvc.perform(get("/api/invoices/{id}/pdf", invoiceId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", startsWith("attachment; filename=\"INV-2026-0001.pdf\"")))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(pdf).startsWith("%PDF".getBytes());

        mockMvc.perform(post("/api/invoices/{id}/mark-paid", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void rejectsInvoiceGenerationWithoutApprovedTimeEntries() throws Exception {
        UUID projectId = createProject();

        mockMvc.perform(post("/api/invoices/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": "%s",
                                  "issueDate": "2026-06-03",
                                  "dueDate": "2026-06-17"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No approved uninvoiced time entries found for project: " + projectId));
    }

    private UUID createApprovedTimeEntry(UUID projectId) throws Exception {
        UUID timeEntryId = createTimeEntry(projectId);

        mockMvc.perform(post("/api/time-entries/{id}/submit", timeEntryId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/time-entries/{id}/approve", timeEntryId))
                .andExpect(status().isOk());

        return timeEntryId;
    }

    private UUID createTimeEntry(UUID projectId) throws Exception {
        String response = mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": "%s",
                                  "workDate": "2026-06-03",
                                  "hours": 7.50,
                                  "description": "Implemented invoice generation"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asText());
    }

    private UUID createProject() throws Exception {
        UUID customerId = createCustomer();
        String response = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "name": "Invoice generation %s",
                                  "description": "Generate invoices from approved time entries",
                                  "hourlyRate": 95.00,
                                  "currency": "EUR",
                                  "status": "ACTIVE",
                                  "startDate": "2026-06-01"
                                }
                                """.formatted(customerId, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asText());
    }

    private UUID createCustomer() throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Invoice Customer %s",
                                  "contactName": "Jane Doe",
                                  "email": "%s@example.com",
                                  "phone": "+31 20 123 4567",
                                  "vatNumber": "NL123456789B01",
                                  "street": "Keizersgracht 1",
                                  "city": "Amsterdam",
                                  "country": "Netherlands"
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asText());
    }
}
