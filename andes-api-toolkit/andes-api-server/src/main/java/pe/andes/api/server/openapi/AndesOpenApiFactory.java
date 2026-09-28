package pe.andes.api.server.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import pe.andes.api.server.config.AndesServerProperties;

import java.util.List;
import java.util.Map;

/**
 * Builds a Swagger/OpenAPI {@link OpenAPI} model from {@link AndesServerProperties.OpenApi}.
 * The resulting bean is automatically picked up by springdoc-openapi (when present on the
 * classpath) to enrich the generated OpenAPI document without overriding the actual API
 * contract defined by the application's endpoints.
 */
public final class AndesOpenApiFactory {

    private AndesOpenApiFactory() {
    }

    public static OpenAPI build(AndesServerProperties.OpenApi props) {
        Info info = new Info()
                .title(props.getTitle())
                .description(props.getDescription())
                .version(props.getVersion());

        if (props.getContact() != null) {
            Contact contact = new Contact()
                    .name(props.getContact().getName())
                    .email(props.getContact().getEmail())
                    .url(props.getContact().getUrl());
            info.setContact(contact);
        }

        if (props.getLicense() != null && props.getLicense().getName() != null) {
            info.setLicense(new License().name(props.getLicense().getName()).url(props.getLicense().getUrl()));
        }

        OpenAPI openAPI = new OpenAPI().info(info);

        List<Server> servers = props.getServers().stream()
                .map(s -> new Server().url(s.getUrl()).description(s.getDescription()))
                .toList();
        if (!servers.isEmpty()) {
            openAPI.setServers(servers);
        }

        List<Tag> tags = props.getTags().stream()
                .map(t -> new Tag().name(t.getName()).description(t.getDescription()))
                .toList();
        if (!tags.isEmpty()) {
            openAPI.setTags(tags);
        }

        for (Map.Entry<String, AndesServerProperties.SecuritySchemeInfo> entry : props.getSecuritySchemes().entrySet()) {
            AndesServerProperties.SecuritySchemeInfo schemeProps = entry.getValue();
            SecurityScheme scheme = new SecurityScheme();
            if (schemeProps.getType() != null) {
                scheme.setType(SecurityScheme.Type.valueOf(schemeProps.getType().toUpperCase()));
            }
            scheme.setScheme(schemeProps.getScheme());
            scheme.setBearerFormat(schemeProps.getBearerFormat());
            scheme.setName(schemeProps.getName());
            if (schemeProps.getIn() != null) {
                scheme.setIn(SecurityScheme.In.valueOf(schemeProps.getIn().toUpperCase()));
            }
            if (openAPI.getComponents() == null) {
                openAPI.setComponents(new Components());
            }
            openAPI.getComponents().addSecuritySchemes(entry.getKey(), scheme);
        }

        return openAPI;
    }
}
