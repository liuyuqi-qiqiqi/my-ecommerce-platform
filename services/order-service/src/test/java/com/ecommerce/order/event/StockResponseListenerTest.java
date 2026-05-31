package com.ecommerce.order.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.IdempotencyService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockResponseListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private StockResponseListener stockResponseListener;

    @Test
    void onStockReservedConfirmsPendingOrder() {
        Order order = pendingOrder();
        when(idempotencyService.registerIfNew("evt-1")).thenReturn(true);
        when(orderRepository.findDetailedById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockResponseListener.onStockReserved(
                new CommerceEvent("evt-1", "StockReserved", Instant.now(), Map.of("orderId", 1L)));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getShipment()).isNotNull();
        verify(orderEventPublisher).publishOrderConfirmed(any(CommerceEvent.class));
    }

    @Test
    void onStockReservationFailedCancelsPendingOrder() {
        Order order = pendingOrder();
        when(idempotencyService.registerIfNew("evt-2")).thenReturn(true);
        when(orderRepository.findDetailedById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockResponseListener.onStockReservationFailed(
                new CommerceEvent("evt-2", "StockReservationFailed", Instant.now(), Map.of("orderId", 1L)));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderEventPublisher).publishOrderCancelled(any(CommerceEvent.class));
    }

    @Test
    void duplicateEventIsIgnored() {
        when(idempotencyService.registerIfNew("evt-dup")).thenReturn(false);

        stockResponseListener.onStockReserved(
                new CommerceEvent("evt-dup", "StockReserved", Instant.now(), Map.of("orderId", 1L)));

        verify(orderRepository, never()).findDetailedById(1L);
    }

    private Order pendingOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-1");
        order.setUserId(9L);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(BigDecimal.TEN);
        order.setShippingFee(BigDecimal.ONE);
        order.setTotalAmount(BigDecimal.valueOf(11));
        order.setPaymentMethod("PAY_ON_DELIVERY");
        order.setAddressSnapshot("{}");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(10L);
        item.setProductName("Phone");
        item.setUnitPrice(BigDecimal.TEN);
        item.setQuantity(1);
        order.getItems().add(item);
        return order;
    }
}
