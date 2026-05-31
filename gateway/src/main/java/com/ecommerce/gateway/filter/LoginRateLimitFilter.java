package com.ecommerce.gateway.filter;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoginRateLimitFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_PATH = "/api/auth/login";

    private final int maxRequests;
    private final long windowMs;
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(
            @Value("${ecommerce.gateway.login-rate-limit.max-requests:20}") int maxRequests,
            @Value("${ecommerce.gateway.login-rate-limit.window-seconds:60}") long windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMs = Duration.ofSeconds(windowSeconds).toMillis();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!isLoginPost(exchange)) {
            return chain.filter(exchange);
        }

        String clientKey = resolveClientKey(exchange);
        if (isRateLimited(clientKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        recordRequest(clientKey);
        return chain.filter(exchange);
    }

    private boolean isLoginPost(ServerWebExchange exchange) {
        return HttpMethod.POST.equals(exchange.getRequest().getMethod())
                && LOGIN_PATH.equals(exchange.getRequest().getURI().getPath());
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null
                && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private boolean isRateLimited(String clientKey) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestLog.computeIfAbsent(clientKey, key -> new ArrayDeque<>());
        synchronized (timestamps) {
            pruneOldEntries(timestamps, now);
            return timestamps.size() >= maxRequests;
        }
    }

    private void recordRequest(String clientKey) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestLog.computeIfAbsent(clientKey, key -> new ArrayDeque<>());
        synchronized (timestamps) {
            pruneOldEntries(timestamps, now);
            timestamps.addLast(now);
        }
    }

    private void pruneOldEntries(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
            timestamps.removeFirst();
        }
    }

    @Override
    public int getOrder() {
        return -110;
    }
}
