package pe.andes.poc.client.orders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.andes.api.common.exception.AndesApiException;
import pe.andes.api.common.model.ApiResponse;
import pe.andes.api.common.util.ApiResponseUtils;
import pe.andes.api.common.util.ErrorUtils;
import pe.andes.poc.client.generated.orders.model.CreateOrderRequest;
import pe.andes.poc.client.generated.orders.model.Order;

/**
 * Demo entry point exercising {@link OrderClientService}. Since this PoC does not use
 * andes-api-server, responses/errors are wrapped manually with andes-api-common utilities
 * to keep the same {@code ApiResponse} contract observed by consumers.
 */
@RestController
@RequestMapping("/api/v1/orders-demo")
public class OrdersDemoController {

    private final OrderClientService orderClientService;

    public OrdersDemoController(OrderClientService orderClientService) {
        this.orderClientService = orderClientService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable String orderId) {
        try {
            return ResponseEntity.ok(ApiResponseUtils.ok(orderClientService.getOrder(orderId)));
        } catch (AndesApiException ex) {
            return ResponseEntity.status(ex.getHttpStatus()).body(ApiResponseUtils.fail(ErrorUtils.toApiError(ex, null)));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseUtils.ok(orderClientService.createOrder(request)));
        } catch (AndesApiException ex) {
            return ResponseEntity.status(ex.getHttpStatus()).body(ApiResponseUtils.fail(ErrorUtils.toApiError(ex, null)));
        }
    }
}
