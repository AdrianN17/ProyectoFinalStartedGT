package pe.edu.galaxy.training.java.gt.creditcard.fraud;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import pe.andes.api.client.AndesApiClient;
import pe.andes.api.client.AndesApiClientRegistry;
import pe.andes.api.common.model.ApiResponse;
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
 * <p>El simulador interno ({@code FraudCheckSimulatorController}) esta envuelto por
 * {@code AndesResponseBodyAdvice} (andes-api-server), que aplica el sobre estandar
 * {@link ApiResponse} a <b>todas</b> las respuestas del proceso (no hay forma de excluir
 * un controlador puntual en la version actual del starter). Por eso aqui se deserializa
 * explicitamente a {@code ApiResponse<FraudCheckResponse>} y se extrae {@code getData()},
 * en vez de mapear {@link FraudCheckResponse} directamente (lo que fallaria con
 * {@code MismatchedInputException} al no encontrar {@code riskScore} en la raiz del JSON).
 * El mock externo independiente ({@code scripts/mock_fraudcheck_server.py}) no tiene este
 * problema porque no pasa por {@code AndesResponseBodyAdvice}; devuelve el JSON "plano" del
 * contrato, por eso {@code base-url} debe apuntar a ese mock para probar el cliente sin el
 * envoltorio del simulador interno (ver README).</p>
 */
@Service
public class FraudCheckClient {

    private static final ParameterizedTypeReference<ApiResponse<FraudCheckResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final AndesApiClient fraudCheckClient;

    public FraudCheckClient(AndesApiClientRegistry registry) {
        this.fraudCheckClient = registry.get("fraudCheck");
    }

    public FraudCheckResponse evaluate(FraudCheckRequest request) {
        ApiResponse<FraudCheckResponse> response =
                fraudCheckClient.post("/internal/fraud-check", request, RESPONSE_TYPE);
        return response.getData();
    }
}
