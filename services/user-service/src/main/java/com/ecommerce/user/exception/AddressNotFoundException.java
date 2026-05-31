package com.ecommerce.user.exception;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(Long addressId) {
        super("Address not found: " + addressId);
    }
}
