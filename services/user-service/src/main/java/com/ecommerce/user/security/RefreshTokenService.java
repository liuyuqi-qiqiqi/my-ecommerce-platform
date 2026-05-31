package com.ecommerce.user.security;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final Duration refreshTtl;

    public RefreshTokenService(
            StringRedisTemplate redisTemplate,
            @Value("${ecommerce.jwt.refresh-token-ttl-days:7}") long refreshTokenTtlDays) {
        this.redisTemplate = redisTemplate;
        this.refreshTtl = Duration.ofDays(refreshTokenTtlDays);
    }

    public String issueRefreshToken(Long userId) {
        String tokenId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key(tokenId), String.valueOf(userId), refreshTtl);
        return tokenId;
    }

    public Long validateAndRotate(String refreshToken) {
        String userId = redisTemplate.opsForValue().get(key(refreshToken));
        if (userId == null) {
            return null;
        }
        revoke(refreshToken);
        return Long.valueOf(userId);
    }

    public void revoke(String refreshToken) {
        redisTemplate.delete(key(refreshToken));
    }

    private String key(String tokenId) {
        return KEY_PREFIX + tokenId;
    }
}
