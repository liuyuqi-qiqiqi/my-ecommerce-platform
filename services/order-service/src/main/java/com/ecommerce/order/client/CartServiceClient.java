package com.ecommerce.order.client;

import com.ecommerce.order.client.dto.AddressDto;
import com.ecommerce.order.client.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service", path = "/internal/cart")
public interface CartServiceClient {

    @GetMapping
    CartDto getCart(@RequestHeader("X-User-Id") Long userId);
}
