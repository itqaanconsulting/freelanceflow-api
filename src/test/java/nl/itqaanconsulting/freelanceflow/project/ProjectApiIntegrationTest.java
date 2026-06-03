package nl.itqaanconsulting.freelanceflow.project;

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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class ProjectApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsProjectForExistingCustomer() throws Exception {
        UUID customerId = createCustomer("project-api@example.com");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "name": "Backend modernization",
                                  "description": "Spring Boot API modernization",
                                  "hourlyRate": 95.00,
                                  "currency": "EUR",
                                  "status": "ACTIVE",
                                  "startDate": "2026-06-01"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.customerName").value("Acme Consulting"))
                .andExpect(jsonPath("$.name").value("Backend modernization"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/projects")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Backend modernization"));
    }

    @Test
    void rejectsProjectForUnknownCustomer() throws Exception {
        UUID unknownCustomerId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "name": "Unknown customer project",
                                  "hourlyRate": 95.00,
                                  "currency": "EUR",
                                  "status": "ACTIVE"
                                }
                                """.formatted(unknownCustomerId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found: " + unknownCustomerId));
    }

    private UUID createCustomer(String email) throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Acme Consulting",
                                  "contactName": "Jane Doe",
                                  "email": "%s",
                                  "phone": "+31 20 123 4567",
                                  "vatNumber": "NL123456789B01",
                                  "street": "Keizersgracht 1",
                                  "city": "Amsterdam",
                                  "country": "Netherlands"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asText());
    }
}
