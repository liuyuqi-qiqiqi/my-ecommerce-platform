package com.ecommerce.bff.web;

import com.ecommerce.bff.client.UserServiceClient;
import com.ecommerce.bff.dto.AddressDto;
import com.ecommerce.bff.dto.AddressRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final UserServiceClient userServiceClient;

    public AddressController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @GetMapping
    public List<AddressDto> listAddresses(@RequestHeader("X-User-Id") Long userId) {
        return userServiceClient.listAddresses(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDto createAddress(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody AddressRequest request) {
        return userServiceClient.createAddress(userId, request);
    }

    @PutMapping("/{addressId}")
    public AddressDto updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return userServiceClient.updateAddress(userId, addressId, request);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(@RequestHeader("X-User-Id") Long userId, @PathVariable Long addressId) {
        userServiceClient.deleteAddress(userId, addressId);
    }
}
