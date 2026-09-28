package pe.andes.poc.integration.orders;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pe.andes.api.common.http.AndesApiConstants;
import pe.andes.api.common.model.ApiMetadata;
import pe.andes.poc.integration.generated.api.CheckoutsApiDelegate;
import pe.andes.poc.integration.generated.model.CheckoutEnvelope;
import pe.andes.poc.integration.generated.model.CheckoutRequest;
import pe.andes.poc.integration.generated.model.CheckoutResult;

/**
 * Business-logic implementation of the {@code CheckoutsApi} contract generated from
 * {@code contracts/openapi-integration.yaml}. The generated {@code CheckoutsApiController}
 * delegates every request here; this is the only hand-written piece of the API surface.
 */
@Service
public class CheckoutApiDelegateImpl implements CheckoutsApiDelegate {

    private final CheckoutService checkoutService;

    public CheckoutApiDelegateImpl(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @Override
    public ResponseEntity<CheckoutEnvelope> createCheckout(CheckoutRequest checkoutRequest) {
        CheckoutResult result = checkoutService.checkout(checkoutRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(successEnvelope(result));
    }

    @Override
    public ResponseEntity<CheckoutEnvelope> getCheckout(String orderId) {
        return ResponseEntity.ok(successEnvelope(checkoutService.findOrder(orderId)));
    }

    private CheckoutEnvelope successEnvelope(CheckoutResult result) {
        return new CheckoutEnvelope().success(true).data(result).metadata(currentMetadata());
    }

    private ApiMetadata currentMetadata() {
        String correlationId = MDC.get(AndesApiConstants.MDC_CORRELATION_ID);
        return ApiMetadata.builder()
                .traceId(correlationId)
                .correlationId(correlationId)
                .requestId(MDC.get(AndesApiConstants.MDC_REQUEST_ID))
                .build();
    }
}
