package com.ecommerce.bff.web;

import com.ecommerce.bff.client.UserServiceClient;
import com.ecommerce.bff.dto.UpdateProfileRequest;
import com.ecommerce.bff.dto.UserProfileDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final UserServiceClient userServiceClient;

    public ProfileController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @GetMapping
    public UserProfileDto getProfile(@RequestHeader("X-User-Id") Long userId) {
        return userServiceClient.getProfile(userId);
    }

    @PutMapping
    public UserProfileDto updateProfile(
            @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody UpdateProfileRequest request) {
        return userServiceClient.updateProfile(userId, request);
    }
}
