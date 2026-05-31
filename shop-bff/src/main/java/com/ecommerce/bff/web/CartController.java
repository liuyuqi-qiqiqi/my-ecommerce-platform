package com.ecommerce.bff.web;

import com.ecommerce.bff.client.CartServiceClient;
import com.ecommerce.bff.dto.AddCartItemRequest;
import com.ecommerce.bff.dto.CartDto;
import com.ecommerce.bff.dto.UpdateCartItemRequest;
import com.ecommerce.bff.support.CartSessionSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartServiceClient cartServiceClient;
    private final CartSessionSupport cartSessionSupport;

    public CartController(CartServiceClient cartServiceClient, CartSessionSupport cartSessionSupport) {
        this.cartServiceClient = cartServiceClient;
        this.cartSessionSupport = cartSessionSupport;
    }

    @GetMapping
    public CartDto getCart(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        String guestSessionId = cartSessionSupport.resolveGuestSessionId(request, response);
        return cartServiceClient.getCart(userId, guestSessionId);
    }

    @PostMapping("/items")
    public CartDto addItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody AddCartItemRequest body) {
        String guestSessionId = cartSessionSupport.resolveGuestSessionId(request, response);
        return cartServiceClient.addItem(userId, guestSessionId, body);
    }

    @PutMapping("/items/{productId}")
    public CartDto updateItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest body) {
        String guestSessionId = cartSessionSupport.resolveGuestSessionId(request, response);
        return cartServiceClient.updateItem(userId, guestSessionId, productId, body);
    }

    @DeleteMapping("/items/{productId}")
    public CartDto removeItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long productId) {
        String guestSessionId = cartSessionSupport.resolveGuestSessionId(request, response);
        return cartServiceClient.removeItem(userId, guestSessionId, productId);
    }
}
