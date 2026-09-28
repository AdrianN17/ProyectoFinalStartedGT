package pe.andes.poc.integration.orders;

import org.springframework.stereotype.Service;
import pe.andes.poc.integration.generated.model.CheckoutItem;
import pe.andes.poc.integration.generated.model.CheckoutRequest;
import pe.andes.poc.integration.generated.model.CheckoutResult;
import pe.andes.poc.integration.generated.orders.model.CreateOrderRequest;
import pe.andes.poc.integration.generated.orders.model.Order;
import pe.andes.poc.integration.generated.orders.model.OrderItem;

/**
 * The "Business logic" step of the checkout flow, sitting between the own exposed API
 * ({@link CheckoutApiDelegateImpl}, generated from {@code openapi-integration.yaml}) and the
 * remote API call ({@link OrdersRemoteService}, generated from {@code openapi-client-a.yaml}).
 * Owns the mapping between the two independently-generated, differently-shaped contracts.
 */
@Service
public class CheckoutService {

    private final OrdersRemoteService ordersRemoteService;

    public CheckoutService(OrdersRemoteService ordersRemoteService) {
        this.ordersRemoteService = ordersRemoteService;
    }

    public CheckoutResult checkout(CheckoutRequest request) {
        // Business rules would be applied here (pricing, fraud checks, etc.) before
        // delegating the actual order placement to the remote Orders API.
        CreateOrderRequest remoteRequest = new CreateOrderRequest()
                .customerId(request.getCustomerId())
                .items(request.getItems().stream().map(this::toRemoteItem).toList());
        Order remoteOrder = ordersRemoteService.placeOrder(remoteRequest);
        return toCheckoutResult(remoteOrder);
    }

    public CheckoutResult findOrder(String orderId) {
        return toCheckoutResult(ordersRemoteService.getOrder(orderId));
    }

    private OrderItem toRemoteItem(CheckoutItem item) {
        return new OrderItem().sku(item.getSku()).quantity(item.getQuantity()).unitPrice(item.getUnitPrice());
    }

    private CheckoutItem toCheckoutItem(OrderItem item) {
        return new CheckoutItem().sku(item.getSku()).quantity(item.getQuantity()).unitPrice(item.getUnitPrice());
    }

    private CheckoutResult toCheckoutResult(Order order) {
        return new CheckoutResult()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus() != null ? order.getStatus().getValue() : null)
                .items(order.getItems().stream().map(this::toCheckoutItem).toList());
    }
}
