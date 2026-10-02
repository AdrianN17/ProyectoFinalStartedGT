package pe.edu.galaxy.training.java.gt.creditcard.fraud;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.galaxy.training.java.gt.creditcard.fraud.generated.FraudCheckRequest;
import pe.edu.galaxy.training.java.gt.creditcard.fraud.generated.FraudCheckResponse;

/**
 * Simulador local del servicio externo de scoring de fraude (patron usado en andes-api-toolkit:
 * examples/poc-client), para que este proyecto sea autocontenido y no dependa de un tercero real.
 * El riesgo crece con el monto de la transaccion. Implementa el mismo contrato
 * (contracts/openapi-fraudcheck.yaml) que scripts/mock_fraudcheck_server.py.
 */
@RestController
@RequestMapping("/internal/fraud-check")
public class FraudCheckSimulatorController {

    private static final BigDecimal HIGH_RISK_AMOUNT = BigDecimal.valueOf(3000);
    private static final BigDecimal MEDIUM_RISK_AMOUNT = BigDecimal.valueOf(1000);

    @PostMapping
    public FraudCheckResponse evaluate(@RequestBody FraudCheckRequest request) {
        BigDecimal amount = request.getAmount();
        int riskScore;
        if (amount.compareTo(HIGH_RISK_AMOUNT) > 0) {
            riskScore = 90;
        } else if (amount.compareTo(MEDIUM_RISK_AMOUNT) > 0) {
            riskScore = 50;
        } else {
            riskScore = 10;
        }
        return new FraudCheckResponse().riskScore(riskScore);
    }
}
