package com.ecommerce.bff.dto;

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
