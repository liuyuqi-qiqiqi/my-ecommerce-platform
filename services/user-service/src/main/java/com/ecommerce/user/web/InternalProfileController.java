package com.ecommerce.user.web;

import com.ecommerce.user.dto.UpdateProfileRequest;
import com.ecommerce.user.dto.UserProfileDto;
import com.ecommerce.user.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/profile")
public class InternalProfileController {

    private final ProfileService profileService;

    public InternalProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserProfileDto getProfile(@RequestHeader("X-User-Id") Long userId) {
        return profileService.getProfile(userId);
    }

    @PutMapping
    public UserProfileDto updateProfile(
            @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(userId, request);
    }
}
