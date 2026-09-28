package pe.andes.api.server.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import pe.andes.api.server.config.AndesServerProperties;

import static org.junit.jupiter.api.Assertions.*;

class AndesOpenApiFactoryTest {

    @Test
    void buildsInfoFromProperties() {
        AndesServerProperties.OpenApi props = new AndesServerProperties.OpenApi();
        props.setTitle("Customer API");
        props.setVersion("2.0.0");
        props.getContact().setName("Andes Team");

        OpenAPI openAPI = AndesOpenApiFactory.build(props);

        assertEquals("Customer API", openAPI.getInfo().getTitle());
        assertEquals("2.0.0", openAPI.getInfo().getVersion());
        assertEquals("Andes Team", openAPI.getInfo().getContact().getName());
    }

    @Test
    void buildsSecuritySchemes() {
        AndesServerProperties.OpenApi props = new AndesServerProperties.OpenApi();
        AndesServerProperties.SecuritySchemeInfo bearer = new AndesServerProperties.SecuritySchemeInfo();
        bearer.setType("http");
        bearer.setScheme("bearer");
        bearer.setBearerFormat("JWT");
        props.getSecuritySchemes().put("bearerAuth", bearer);

        OpenAPI openAPI = AndesOpenApiFactory.build(props);

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
    }
}
