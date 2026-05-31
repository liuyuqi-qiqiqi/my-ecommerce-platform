package com.ecommerce.order.client;

import com.ecommerce.order.client.dto.AddressDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", path = "/internal/addresses")
public interface UserAddressClient {

    @GetMapping("/{addressId}")
    AddressDto getAddress(@RequestHeader("X-User-Id") Long userId, @PathVariable("addressId") Long addressId);
}
