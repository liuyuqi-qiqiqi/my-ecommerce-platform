package com.ecommerce.bff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100) String displayName, @Size(max = 20) String phone, @Email String email) {}
