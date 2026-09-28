package pe.andes.api.client.web;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import pe.andes.api.client.config.AndesClientProperties;
import pe.andes.api.client.error.AndesClientErrorMapper;
import pe.andes.api.common.exception.AndesNotFoundException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class AndesRestClientFactoryTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void successfulCallReturnsBody() {
        stubFor(get(urlEqualTo("/customers/1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"name\":\"Ada\"}")));

        RestClient restClient = buildClient();
        Map<?, ?> body = restClient.get().uri("/customers/1").retrieve().body(Map.class);

        assertEquals(1, body.get("id"));
        assertEquals("Ada", body.get("name"));
    }

    @Test
    void notFoundIsMappedToAndesNotFoundException() {
        stubFor(get(urlEqualTo("/customers/99"))
                .willReturn(aResponse().withStatus(404).withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":\"NOT_FOUND\",\"message\":\"missing\",\"httpStatus\":404}")));

        RestClient restClient = buildClient();

        AndesNotFoundException ex = assertThrows(AndesNotFoundException.class,
                () -> restClient.get().uri("/customers/99").retrieve().body(Map.class));
        assertEquals("missing", ex.getMessage());
    }

    private RestClient buildClient() {
        AndesClientProperties.ClientConfig config = new AndesClientProperties.ClientConfig();
        config.setBaseUrl("http://localhost:" + wireMockServer.port());
        config.setConnectTimeout(Duration.ofSeconds(2));
        config.setReadTimeout(Duration.ofSeconds(2));

        AndesRestClientFactory factory = new AndesRestClientFactory(new AndesClientErrorMapper());
        return factory.createClient("test", config, List.of());
    }
}
