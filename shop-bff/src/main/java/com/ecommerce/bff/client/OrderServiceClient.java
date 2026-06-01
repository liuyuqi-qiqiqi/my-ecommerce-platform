package com.ecommerce.bff.client;

import com.ecommerce.bff.dto.CheckoutRequest;
import com.ecommerce.bff.dto.OrderDetailDto;
import com.ecommerce.bff.dto.OrderPageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", path = "/internal/orders")
public interface OrderServiceClient {

    @PostMapping("/checkout")
    OrderDetailDto checkout(@RequestHeader("X-User-Id") Long userId, @RequestBody CheckoutRequest request);

    @GetMapping
    OrderPageDto listOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize);

    @GetMapping("/{orderId}")
    OrderDetailDto getOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable Long orderId);

    @PostMapping("/{orderId}/cancel")
    void cancelOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable Long orderId);
}
