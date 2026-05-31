package com.ecommerce.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

class LoginRateLimitFilterTest {

    private LoginRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new LoginRateLimitFilter(2, 60);
    }

    @Test
    void allowsRequestsUnderLimit() {
        WebFilterChain chain = exchange -> Mono.empty();

        ServerWebExchange first = loginExchange("1.2.3.4");
        ServerWebExchange second = loginExchange("1.2.3.4");

        filter.filter(first, chain).block();
        filter.filter(second, chain).block();

        assertThat(first.getResponse().getStatusCode()).isNull();
        assertThat(second.getResponse().getStatusCode()).isNull();
    }

    @Test
    void blocksRequestsOverLimit() {
        WebFilterChain chain = exchange -> Mono.empty();

        filter.filter(loginExchange("5.6.7.8"), chain).block();
        filter.filter(loginExchange("5.6.7.8"), chain).block();
        ServerWebExchange blocked = loginExchange("5.6.7.8");
        filter.filter(blocked, chain).block();

        assertThat(blocked.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void ignoresNonLoginRoutes() {
        WebFilterChain chain = exchange -> Mono.empty();
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/api/products/featured")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    private ServerWebExchange loginExchange(String ip) {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, "/api/auth/login")
                .remoteAddress(new InetSocketAddress(ip, 12345))
                .build();
        return MockServerWebExchange.from(request);
    }
}
