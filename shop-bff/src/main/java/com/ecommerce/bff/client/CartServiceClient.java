package com.ecommerce.bff.client;

import com.ecommerce.bff.dto.AddCartItemRequest;
import com.ecommerce.bff.dto.CartDto;
import com.ecommerce.bff.dto.UpdateCartItemRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service", path = "/internal/cart")
public interface CartServiceClient {

    @GetMapping
    CartDto getCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId);

    @PostMapping("/items")
    CartDto addItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId,
            @RequestBody AddCartItemRequest request);

    @PutMapping("/items/{productId}")
    CartDto updateItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId,
            @PathVariable("productId") Long productId,
            @RequestBody UpdateCartItemRequest request);

    @DeleteMapping("/items/{productId}")
    CartDto removeItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId,
            @PathVariable("productId") Long productId);
}
