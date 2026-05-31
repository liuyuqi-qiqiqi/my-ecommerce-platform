package com.ecommerce.order.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate(Long orderId) {
        return "ORD-" + LocalDate.now().format(DATE_FORMAT) + "-" + String.format("%06d", orderId);
    }
}
