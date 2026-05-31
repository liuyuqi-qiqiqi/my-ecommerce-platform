package com.ecommerce.user.dto;

public record AddressDto(
        Long id,
        String recipientName,
        String phone,
        String province,
        String city,
        String district,
        String street,
        String postalCode,
        boolean isDefault) {}
