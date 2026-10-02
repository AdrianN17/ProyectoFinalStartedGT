package pe.edu.galaxy.training.java.gt.creditcard.fraud;

import org.springframework.stereotype.Service;

import pe.andes.api.client.AndesApiClient;
import pe.andes.api.client.AndesApiClientRegistry;
import pe.edu.galaxy.training.java.gt.creditcard.fraud.generated.FraudCheckRequest;
import pe.edu.galaxy.training.java.gt.creditcard.fraud.generated.FraudCheckResponse;

/**
 * Consume el servicio externo de scoring de fraude a traves del cliente nombrado "fraudCheck"
 * (andes-api-client-spring-boot-starter): timeouts, correlation id y mapeo de errores HTTP a
 * AndesApiException los aporta el starter, sin escribir codigo de plomeria HTTP a mano.
 *
 * <p>Este es el segundo contrato del proyecto (contracts/openapi-fraudcheck.yaml), consumido
 * como cliente. {@link FraudCheckRequest}/{@link FraudCheckResponse} son modelos generados por
 * openapi-generator-gradle-plugin (tarea {@code openApiGenerate}) a partir de ese contrato -
 * no se escriben a mano, igual que el patron usado en andes-api-toolkit/examples/poc-client.</p>
 *
 * <p>La demo consume el mock externo independiente ({@code scripts/mock_fraudcheck_server.py}),
 * que devuelve el JSON plano del contrato ({@code {"riskScore": ...}}), por lo que el
 * cliente deserializa directamente a {@link FraudCheckResponse}.</p>
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
