package pe.andes.api.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

/**
 * Thin, typed convenience wrapper around a configured {@link RestClient} for a named
 * remote API (e.g. "customer", "payment"). Serialization, headers, timeouts and error
 * mapping are already applied by the underlying {@code RestClient}; this class only
 * standardizes the call sites used by application code.
 */
public class AndesApiClient {

    private final String name;
    private final RestClient restClient;

    public AndesApiClient(String name, RestClient restClient) {
        this.name = name;
        this.restClient = restClient;
    }

    public String getName() {
        return name;
    }

    /**
     * Exposes the underlying {@link RestClient} for advanced use cases (multipart, streaming, etc).
     */
    public RestClient raw() {
        return restClient;
    }

    public <T> T get(String uri, Class<T> responseType) {
        return restClient.get().uri(uri).retrieve().body(responseType);
    }

    public <T> T get(String uri, ParameterizedTypeReference<T> responseType) {
        return restClient.get().uri(uri).retrieve().body(responseType);
    }

    public <T> T post(String uri, Object body, Class<T> responseType) {
        return restClient.post().uri(uri).body(body).retrieve().body(responseType);
    }

    public <T> T post(String uri, Object body, ParameterizedTypeReference<T> responseType) {
        return restClient.post().uri(uri).body(body).retrieve().body(responseType);
    }

    public <T> T put(String uri, Object body, Class<T> responseType) {
        return restClient.put().uri(uri).body(body).retrieve().body(responseType);
    }

    public <T> T patch(String uri, Object body, Class<T> responseType) {
        return restClient.patch().uri(uri).body(body).retrieve().body(responseType);
    }

    public void delete(String uri) {
        restClient.delete().uri(uri).retrieve().toBodilessEntity();
    }
}
