package pe.andes.poc.integration.orders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.andes.poc.integration.generated.orders.model.CreateOrderRequest;
import pe.andes.poc.integration.generated.orders.model.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local stand-in for the external Orders API (see {@code contracts/openapi-client-a.yaml}),
 * representing "Remote API" in the checkout flow: Own API -> Business logic -> Client Library
 * -> Remote API -> Response -> Standardized response.
 */
@RestController
@RequestMapping("/v2/orders")
public class ExternalOrdersSimulatorController {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errorCode", "ORDER_NOT_FOUND", "errorMessage", "Order " + orderId + " not found"));
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = "ORD-" + sequence.incrementAndGet();
        double total = request.getItems().stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        Order order = new Order()
                .orderId(orderId)
                .customerId(request.getCustomerId())
                .totalAmount(total)
                .status(Order.StatusEnum.PENDING)
                .items(request.getItems());
        orders.put(orderId, order);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}

