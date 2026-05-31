package com.ecommerce.bff.client;

import com.ecommerce.bff.dto.AddressDto;
import com.ecommerce.bff.dto.AddressRequest;
import com.ecommerce.bff.dto.AuthResponse;
import com.ecommerce.bff.dto.LoginRequest;
import com.ecommerce.bff.dto.RefreshTokenRequest;
import com.ecommerce.bff.dto.RegisterRequest;
import com.ecommerce.bff.dto.UpdateProfileRequest;
import com.ecommerce.bff.dto.UserProfileDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping("/internal/auth/register")
    AuthResponse register(@RequestBody RegisterRequest request);

    @PostMapping("/internal/auth/login")
    AuthResponse login(@RequestBody LoginRequest request);

    @PostMapping("/internal/auth/refresh")
    AuthResponse refresh(@RequestBody RefreshTokenRequest request);

    @PostMapping("/internal/auth/logout")
    void logout(@RequestHeader("X-User-Id") Long userId, @RequestBody RefreshTokenRequest request);

    @GetMapping("/internal/profile")
    UserProfileDto getProfile(@RequestHeader("X-User-Id") Long userId);

    @PutMapping("/internal/profile")
    UserProfileDto updateProfile(@RequestHeader("X-User-Id") Long userId, @RequestBody UpdateProfileRequest request);

    @GetMapping("/internal/addresses")
    List<AddressDto> listAddresses(@RequestHeader("X-User-Id") Long userId);

    @PostMapping("/internal/addresses")
    AddressDto createAddress(@RequestHeader("X-User-Id") Long userId, @RequestBody AddressRequest request);

    @PutMapping("/internal/addresses/{addressId}")
    AddressDto updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId,
            @RequestBody AddressRequest request);

    @DeleteMapping("/internal/addresses/{addressId}")
    void deleteAddress(@RequestHeader("X-User-Id") Long userId, @PathVariable Long addressId);
}
