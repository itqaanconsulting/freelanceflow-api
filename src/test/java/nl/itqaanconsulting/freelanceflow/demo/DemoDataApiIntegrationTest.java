package nl.itqaanconsulting.freelanceflow.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql(
        statements = {
                "DELETE FROM invoice_lines",
                "UPDATE time_entries SET invoice_id = NULL",
                "DELETE FROM audit_events",
                "DELETE FROM invoices",
                "DELETE FROM time_entries",
                "DELETE FROM projects",
                "DELETE FROM customers"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class DemoDataApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetsDemoDataForPortfolioWalkthrough() throws Exception {
        mockMvc.perform(post("/api/demo/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeEntries").value(3))
                .andExpect(jsonPath("$.auditEvents").value(7));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].companyName").value("Acme Consulting"));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Backend Modernization"));

        mockMvc.perform(get("/api/time-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ISSUED"));

        mockMvc.perform(get("/api/audit-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));
    }
}
