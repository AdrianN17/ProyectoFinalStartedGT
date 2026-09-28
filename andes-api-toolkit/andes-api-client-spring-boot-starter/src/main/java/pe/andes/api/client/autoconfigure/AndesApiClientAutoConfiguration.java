package pe.andes.api.client.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import pe.andes.api.client.AndesApiClient;
import pe.andes.api.client.AndesApiClientRegistry;
import pe.andes.api.client.config.AndesClientProperties;
import pe.andes.api.client.error.AndesClientErrorMapper;
import pe.andes.api.client.web.AndesRestClientCustomizer;
import pe.andes.api.client.web.AndesRestClientFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-registers one {@link AndesApiClient} per entry configured under
 * {@code andes.api.client.clients.*}, exposed through an {@link AndesApiClientRegistry} bean.
 * Every configured client automatically gets: base URL, connect/read timeouts, correlation id,
 * request id, content type headers and HTTP-error-to-{@code AndesApiException} mapping.
 */
@AutoConfiguration
@EnableConfigurationProperties(AndesClientProperties.class)
public class AndesApiClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AndesClientErrorMapper andesClientErrorMapper() {
        return new AndesClientErrorMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public AndesRestClientFactory andesRestClientFactory(AndesClientErrorMapper andesClientErrorMapper) {
        return new AndesRestClientFactory(andesClientErrorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public AndesApiClientRegistry andesApiClientRegistry(AndesClientProperties properties,
                                                            AndesRestClientFactory factory,
                                                            List<AndesRestClientCustomizer> customizers) {
        Map<String, AndesApiClient> clients = new LinkedHashMap<>();
        properties.getClients().forEach((name, config) -> {
            RestClient restClient = factory.createClient(name, config, customizers);
            clients.put(name, new AndesApiClient(name, restClient));
        });
        return new AndesApiClientRegistry(clients);
    }
}
