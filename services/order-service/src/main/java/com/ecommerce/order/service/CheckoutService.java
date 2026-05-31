package com.ecommerce.order.service;

import com.ecommerce.order.client.CartServiceClient;
import com.ecommerce.order.client.UserAddressClient;
import com.ecommerce.order.client.dto.AddressDto;
import com.ecommerce.order.client.dto.CartDto;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.domain.Shipment;
import com.ecommerce.order.domain.ShipmentStatus;
import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderDetailDto;
import com.ecommerce.order.event.CommerceEvent;
import com.ecommerce.order.event.OrderEventPublisher;
import com.ecommerce.order.exception.StockConflictException;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CartServiceClient cartServiceClient;
    private final UserAddressClient userAddressClient;
    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final ShippingFeeCalculator shippingFeeCalculator;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;
    private final long checkoutWaitMs;

    public CheckoutService(
            CartServiceClient cartServiceClient,
            UserAddressClient userAddressClient,
            OrderRepository orderRepository,
            OrderNumberGenerator orderNumberGenerator,
            ShippingFeeCalculator shippingFeeCalculator,
            OrderEventPublisher orderEventPublisher,
            OrderMapper orderMapper,
            ObjectMapper objectMapper,
            @Value("${ecommerce.checkout.wait-ms:5000}") long checkoutWaitMs) {
        this.cartServiceClient = cartServiceClient;
        this.userAddressClient = userAddressClient;
        this.orderRepository = orderRepository;
        this.orderNumberGenerator = orderNumberGenerator;
        this.shippingFeeCalculator = shippingFeeCalculator;
        this.orderEventPublisher = orderEventPublisher;
        this.orderMapper = orderMapper;
        this.objectMapper = objectMapper;
        this.checkoutWaitMs = checkoutWaitMs;
    }

    public OrderDetailDto checkout(Long userId, CheckoutRequest request) {
        Long orderId = createPendingOrder(userId, request);
        Order completed = waitForTerminalState(orderId);
        if (completed.getStatus() == OrderStatus.CANCELLED) {
            throw new StockConflictException(List.of(Map.of("orderId", completed.getId())));
        }
        log.info("AUDIT checkout_confirmed userId={} orderId={}", userId, completed.getId());
        return orderMapper.toDetail(completed);
    }

    @Transactional
    protected Long createPendingOrder(Long userId, CheckoutRequest request) {
        CartDto cart = cartServiceClient.getCart(userId);
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        if (cart.items().stream().anyMatch(item -> !item.available())) {
            throw new IllegalArgumentException("Cart contains unavailable items");
        }

        AddressDto address = userAddressClient.getAddress(userId, request.addressId());
        BigDecimal shippingFee = shippingFeeCalculator.calculate(address);
        BigDecimal total = cart.subtotal().add(shippingFee);

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(cart.subtotal());
        order.setShippingFee(shippingFee);
        order.setTotalAmount(total);
        order.setAddressSnapshot(toJson(address));
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        for (CartDto.CartItemDto item : cart.items()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(item.productId());
            orderItem.setProductName(item.productName());
            orderItem.setUnitPrice(item.unitPrice());
            orderItem.setQuantity(item.quantity());
            order.getItems().add(orderItem);
        }

        Order saved = orderRepository.save(order);
        saved.setOrderNumber(orderNumberGenerator.generate(saved.getId()));
        saved = orderRepository.save(saved);

        List<Map<String, Object>> eventItems = saved.getItems().stream()
                .map(item -> Map.<String, Object>of(
                        "productId", item.getProductId(),
                        "quantity", item.getQuantity(),
                        "unitPrice", item.getUnitPrice()))
                .toList();

        orderEventPublisher.publishOrderCreated(
                CommerceEvent.orderCreated(saved.getId(), saved.getOrderNumber(), userId, eventItems));
        log.info("AUDIT checkout_started userId={} orderId={} orderNumber={}", userId, saved.getId(), saved.getOrderNumber());
        return saved.getId();
    }

    private Order waitForTerminalState(Long orderId) {
        long deadline = System.currentTimeMillis() + checkoutWaitMs;
        while (System.currentTimeMillis() < deadline) {
            Order order = orderRepository.findDetailedById(orderId).orElseThrow();
            if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.CANCELLED) {
                return order;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return orderRepository.findDetailedById(orderId).orElseThrow();
    }

    private String toJson(AddressDto address) {
        Map<String, String> snapshot = new HashMap<>();
        snapshot.put("recipientName", address.recipientName());
        snapshot.put("phone", address.phone());
        snapshot.put("province", address.province());
        snapshot.put("city", address.city());
        snapshot.put("district", address.district());
        snapshot.put("street", address.street());
        snapshot.put("postalCode", address.postalCode());
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize address snapshot", ex);
        }
    }
}
