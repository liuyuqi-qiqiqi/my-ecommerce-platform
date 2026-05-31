package com.ecommerce.user.web;

import com.ecommerce.user.dto.AddressDto;
import com.ecommerce.user.dto.AddressRequest;
import com.ecommerce.user.service.AddressService;
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
@RequestMapping("/internal/addresses")
public class InternalAddressController {

    private final AddressService addressService;

    public InternalAddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<AddressDto> listAddresses(@RequestHeader("X-User-Id") Long userId) {
        return addressService.listAddresses(userId);
    }

    @GetMapping("/{addressId}")
    public AddressDto getAddress(@RequestHeader("X-User-Id") Long userId, @PathVariable Long addressId) {
        return addressService.getAddress(userId, addressId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDto createAddress(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody AddressRequest request) {
        return addressService.createAddress(userId, request);
    }

    @PutMapping("/{addressId}")
    public AddressDto updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return addressService.updateAddress(userId, addressId, request);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(@RequestHeader("X-User-Id") Long userId, @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
    }
}
