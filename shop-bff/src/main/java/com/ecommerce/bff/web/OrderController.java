package com.ecommerce.bff.web;

import com.ecommerce.bff.client.OrderServiceClient;
import com.ecommerce.bff.dto.CheckoutRequest;
import com.ecommerce.bff.dto.OrderDetailDto;
import com.ecommerce.bff.dto.OrderPageDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderServiceClient orderServiceClient;

    public OrderController(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailDto checkout(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CheckoutRequest request) {
        return orderServiceClient.checkout(userId, request);
    }

    @GetMapping
    public OrderPageDto listOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return orderServiceClient.listOrders(userId, page, pageSize);
    }

    @GetMapping("/{orderId}")
    public OrderDetailDto getOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable Long orderId) {
        return orderServiceClient.getOrder(userId, orderId);
    }

    @PostMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable Long orderId) {
        orderServiceClient.cancelOrder(userId, orderId);
    }
}
