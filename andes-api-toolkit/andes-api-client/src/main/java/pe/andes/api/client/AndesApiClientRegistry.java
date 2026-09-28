package pe.andes.api.client;

import java.util.Map;

/**
 * Holds every configured {@link AndesApiClient}, keyed by logical client name, so
 * application code (or generic infrastructure) can resolve a client dynamically.
 */
public class AndesApiClientRegistry {

    private final Map<String, AndesApiClient> clients;

    public AndesApiClientRegistry(Map<String, AndesApiClient> clients) {
        this.clients = Map.copyOf(clients);
    }

    public AndesApiClient get(String name) {
        AndesApiClient client = clients.get(name);
        if (client == null) {
            throw new IllegalArgumentException("No Andes API client configured with name: " + name);
        }
        return client;
    }

    public Map<String, AndesApiClient> getAll() {
        return clients;
    }
}
