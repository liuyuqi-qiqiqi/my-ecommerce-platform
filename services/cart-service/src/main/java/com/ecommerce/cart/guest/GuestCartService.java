package com.ecommerce.cart.guest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GuestCartService {

    private static final String KEY_PREFIX = "guest-cart:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration guestCartTtl;

    public GuestCartService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${ecommerce.cart.guest-ttl-hours:24}") long guestTtlHours) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.guestCartTtl = Duration.ofHours(guestTtlHours);
    }

    public List<GuestCartItem> getItems(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return List.of();
        }
        return read(sessionId).orElseGet(ArrayList::new);
    }

    public void saveItems(String sessionId, List<GuestCartItem> items) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        if (items.isEmpty()) {
            redisTemplate.delete(key(sessionId));
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(items);
            redisTemplate.opsForValue().set(key(sessionId), json, guestCartTtl);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to persist guest cart", ex);
        }
    }

    public void clear(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            redisTemplate.delete(key(sessionId));
        }
    }

    private Optional<List<GuestCartItem>> read(String sessionId) {
        String json = redisTemplate.opsForValue().get(key(sessionId));
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, new TypeReference<List<GuestCartItem>>() {}));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    public record GuestCartItem(Long productId, String productName, java.math.BigDecimal unitPrice, int quantity) {}
}
