package com.ecommerce.order.web;

import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderDetailDto;
import com.ecommerce.order.dto.OrderPageDto;
import com.ecommerce.order.service.CancelOrderService;
import com.ecommerce.order.service.CheckoutService;
import com.ecommerce.order.service.OrderQueryService;
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
@RequestMapping("/internal/orders")
public class InternalOrderController {

    private final CheckoutService checkoutService;
    private final OrderQueryService orderQueryService;
    private final CancelOrderService cancelOrderService;

    public InternalOrderController(
            CheckoutService checkoutService,
            OrderQueryService orderQueryService,
            CancelOrderService cancelOrderService) {
        this.checkoutService = checkoutService;
        this.orderQueryService = orderQueryService;
        this.cancelOrderService = cancelOrderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailDto checkout(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(userId, request);
    }

    @GetMapping
    public OrderPageDto listOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return orderQueryService.listOrders(userId, page, pageSize);
    }

    @GetMapping("/{orderId}")
    public OrderDetailDto getOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable Long orderId) {
        return orderQueryService.getOrder(userId, orderId);
    }

    @PostMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable Long orderId) {
        cancelOrderService.cancelOrder(userId, orderId);
    }
}
