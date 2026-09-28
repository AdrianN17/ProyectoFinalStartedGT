package pe.andes.api.client.web;

import org.springframework.web.client.RestClient;

/**
 * Extension point to customize a named client's {@link RestClient.Builder} (e.g. to add
 * authentication, extra interceptors, or a custom message converter). Implementations
 * registered as Spring beans are applied by the client autoconfiguration to every client,
 * receiving the logical client name so they can apply conditional logic.
 */
@FunctionalInterface
public interface AndesRestClientCustomizer {

    void customize(String clientName, RestClient.Builder builder);
}
