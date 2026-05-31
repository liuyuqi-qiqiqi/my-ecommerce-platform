package com.ecommerce.bff.dto;

public record AuthResponse(String accessToken, String refreshToken, int expiresIn) {}
