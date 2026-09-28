package pe.edu.galaxy.training.java.gt.creditcard.fraud;

import org.springframework.stereotype.Service;

import pe.andes.api.client.AndesApiClient;
import pe.andes.api.client.AndesApiClientRegistry;

/**
 * Consume el servicio externo de scoring de fraude a traves del cliente nombrado "fraudCheck"
 * (andes-api-client-spring-boot-starter): timeouts, correlation id y mapeo de errores HTTP a
 * AndesApiException los aporta el starter, sin escribir codigo de plomeria HTTP a mano.
 */
@Service
public class FraudCheckClient {

    private final AndesApiClient fraudCheckClient;

    public FraudCheckClient(AndesApiClientRegistry registry) {
        this.fraudCheckClient = registry.get("fraudCheck");
    }

    public FraudCheckResponse evaluate(FraudCheckRequest request) {
        return fraudCheckClient.post("/internal/fraud-check", request, FraudCheckResponse.class);
    }
}
