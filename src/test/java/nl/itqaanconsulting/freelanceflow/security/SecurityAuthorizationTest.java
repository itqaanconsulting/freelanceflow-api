package nl.itqaanconsulting.freelanceflow.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsApiRequestsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "FREELANCER")
    void allowsFreelancerToAccessCustomerApi() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FREELANCER")
    void rejectsFreelancerFromAuditEvents() throws Exception {
        mockMvc.perform(get("/api/audit-events"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminToAccessAuditEvents() throws Exception {
        mockMvc.perform(get("/api/audit-events"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FREELANCER")
    void rejectsFreelancerFromMarkingInvoicesPaid() throws Exception {
        mockMvc.perform(post("/api/invoices/00000000-0000-0000-0000-000000000000/mark-paid"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ACCOUNTANT")
    void allowsAccountantThroughInvoicePaymentAuthorization() throws Exception {
        mockMvc.perform(post("/api/invoices/00000000-0000-0000-0000-000000000000/mark-paid"))
                .andExpect(status().isNotFound());
    }
}
