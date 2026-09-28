package pe.andes.api.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Root configuration properties for andes-api-server, bound under {@code andes.api.server}.
 */
@ConfigurationProperties(prefix = "andes.api.server")
public class AndesServerProperties {

    /**
     * Controls automatic wrapping of controller return values into {@code ApiResponse}.
     */
    private final Response response = new Response();

    /**
     * Controls the centralized {@code @RestControllerAdvice} error handling.
     */
    private final ErrorHandling errorHandling = new ErrorHandling();

    /**
     * Controls the correlation id / request id filter.
     */
    private final Correlation correlation = new Correlation();

    /**
     * OpenAPI document metadata.
     */
    private final OpenApi openapi = new OpenApi();

    public Response getResponse() {
        return response;
    }

    public ErrorHandling getErrorHandling() {
        return errorHandling;
    }

    public Correlation getCorrelation() {
        return correlation;
    }

    public OpenApi getOpenapi() {
        return openapi;
    }

    public static class Response {
        /** Whether raw controller return values should be wrapped into {@code ApiResponse}. */
        private boolean wrapEnabled = true;

        public boolean isWrapEnabled() {
            return wrapEnabled;
        }

        public void setWrapEnabled(boolean wrapEnabled) {
            this.wrapEnabled = wrapEnabled;
        }
    }

    public static class ErrorHandling {
        /** Whether the built-in {@code @RestControllerAdvice} should be registered. */
        private boolean enabled = true;
        /** Whether stack traces should be included in error responses (never enable in production). */
        private boolean includeStackTrace = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeStackTrace() {
            return includeStackTrace;
        }

        public void setIncludeStackTrace(boolean includeStackTrace) {
            this.includeStackTrace = includeStackTrace;
        }
    }

    public static class Correlation {
        /** Whether the correlation/request id filter should be registered. */
        private boolean enabled = true;
        /** Whether the server is allowed to generate a correlation id when the caller does not send one. */
        private boolean generateIfMissing = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isGenerateIfMissing() {
            return generateIfMissing;
        }

        public void setGenerateIfMissing(boolean generateIfMissing) {
            this.generateIfMissing = generateIfMissing;
        }
    }

    public static class OpenApi {
        private boolean enabled = true;
        private String title = "Andes API";
        private String description = "";
        private String version = "1.0.0";
        private Contact contact = new Contact();
        private License license = new License();
        private List<ServerInfo> servers = new ArrayList<>();
        private List<TagInfo> tags = new ArrayList<>();
        private Map<String, SecuritySchemeInfo> securitySchemes = new LinkedHashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Contact getContact() {
            return contact;
        }

        public void setContact(Contact contact) {
            this.contact = contact;
        }

        public License getLicense() {
            return license;
        }

        public void setLicense(License license) {
            this.license = license;
        }

        public List<ServerInfo> getServers() {
            return servers;
        }

        public void setServers(List<ServerInfo> servers) {
            this.servers = servers;
        }

        public List<TagInfo> getTags() {
            return tags;
        }

        public void setTags(List<TagInfo> tags) {
            this.tags = tags;
        }

        public Map<String, SecuritySchemeInfo> getSecuritySchemes() {
            return securitySchemes;
        }

        public void setSecuritySchemes(Map<String, SecuritySchemeInfo> securitySchemes) {
            this.securitySchemes = securitySchemes;
        }
    }

    public static class Contact {
        private String name;
        private String email;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class License {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class ServerInfo {
        private String url;
        private String description;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class TagInfo {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class SecuritySchemeInfo {
        /** e.g. "http", "apiKey", "oauth2", "openIdConnect". */
        private String type;
        /** e.g. "bearer", "basic" (used when type=http). */
        private String scheme;
        /** e.g. "JWT" (used when scheme=bearer). */
        private String bearerFormat;
        /** e.g. "header", "query", "cookie" (used when type=apiKey). */
        private String in;
        /** header/query/cookie parameter name (used when type=apiKey). */
        private String name;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getBearerFormat() {
            return bearerFormat;
        }

        public void setBearerFormat(String bearerFormat) {
            this.bearerFormat = bearerFormat;
        }

        public String getIn() {
            return in;
        }

        public void setIn(String in) {
            this.in = in;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
