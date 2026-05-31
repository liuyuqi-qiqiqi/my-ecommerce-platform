package com.ecommerce.cart.event;

import java.time.Instant;
import java.util.Map;

public record CommerceEvent(String eventId, String eventType, Instant timestamp, Map<String, Object> payload) {}
