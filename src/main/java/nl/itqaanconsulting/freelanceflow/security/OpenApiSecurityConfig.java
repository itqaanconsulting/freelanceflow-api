package nl.itqaanconsulting.freelanceflow.security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "FreelanceFlow API",
                version = "0.1.0",
                description = "Freelance time tracking and invoice workflow API"
        ),
        security = @SecurityRequirement(name = "keycloak")
)
@SecurityScheme(
        name = "keycloak",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "http://localhost:8180/realms/freelanceflow/protocol/openid-connect/auth",
                        tokenUrl = "http://localhost:8180/realms/freelanceflow/protocol/openid-connect/token"
                )
        )
)
class OpenApiSecurityConfig {
}
