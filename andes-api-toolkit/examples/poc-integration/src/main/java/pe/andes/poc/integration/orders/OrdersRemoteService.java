package pe.andes.poc.integration.orders;

import org.springframework.stereotype.Service;
import pe.andes.api.client.AndesApiClient;
import pe.andes.api.client.AndesApiClientRegistry;
import pe.andes.poc.integration.generated.orders.model.CreateOrderRequest;
import pe.andes.poc.integration.generated.orders.model.Order;

/**
 * The "Client Library" step of the checkout flow: calls the remote Orders API through the
 * {@code orders} named client (headers, timeouts, serialization and error mapping are all
 * standardized by andes-api-client). {@code Order}/{@code CreateOrderRequest} are generated
 * from {@code contracts/openapi-client-a.yaml}, the external partner's own contract.
 */
@Service
public class OrdersRemoteService {

    private final AndesApiClient ordersClient;

    public OrdersRemoteService(AndesApiClientRegistry registry) {
        this.ordersClient = registry.get("orders");
    }

    public Order getOrder(String orderId) {
        return ordersClient.get("/v2/orders/" + orderId, Order.class);
    }

    public Order placeOrder(CreateOrderRequest request) {
        return ordersClient.post("/v2/orders", request, Order.class);
    }
}
