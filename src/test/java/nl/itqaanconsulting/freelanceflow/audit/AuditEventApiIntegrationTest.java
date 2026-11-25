package nl.itqaanconsulting.freelanceflow.audit;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
class AuditEventApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void recordsAuditEventsForTimeEntryWorkflow() throws Exception {
        UUID projectId = createProject();
        UUID timeEntryId = createTimeEntry(projectId);

        mockMvc.perform(post("/api/time-entries/{id}/submit", timeEntryId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/time-entries/{id}/approve", timeEntryId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/audit-events")
                        .param("aggregateType", "TIME_ENTRY")
                        .param("aggregateId", timeEntryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventType", hasItem("TIME_ENTRY_CREATED")))
                .andExpect(jsonPath("$[*].eventType", hasItem("TIME_ENTRY_SUBMITTED")))
                .andExpect(jsonPath("$[*].eventType", hasItem("TIME_ENTRY_APPROVED")));
    }

    @Test
    @WithMockUser(username = "anna.accountant", roles = "ADMIN")
    void recordsAuthenticatedUserAsAuditActor() throws Exception {
        UUID projectId = createProject();
        UUID timeEntryId = createTimeEntry(projectId);

        mockMvc.perform(get("/api/audit-events")
                        .param("aggregateType", "TIME_ENTRY")
                        .param("aggregateId", timeEntryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("TIME_ENTRY_CREATED"))
                .andExpect(jsonPath("$[0].actor").value("anna.accountant"));
    }

    private UUID createTimeEntry(UUID projectId) throws Exception {
        String response = mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": "%s",
                                  "workDate": "2026-06-03",
                                  "hours": 6.50,
                                  "description": "Implemented audit logging"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("id").asString());
    }

    private UUID createProject() throws Exception {
        UUID customerId = createCustomer();
        String response = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "name": "Audit logging %s",
                                  "description": "Audit workflow changes",
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
        return UUID.fromString(json.get("id").asString());
    }

    private UUID createCustomer() throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Audit Customer %s",
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
        return UUID.fromString(json.get("id").asString());
    }
}
