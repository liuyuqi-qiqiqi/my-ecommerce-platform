package com.ecommerce.user.dto;

public record AuthResponse(String accessToken, String refreshToken, int expiresIn) {}
