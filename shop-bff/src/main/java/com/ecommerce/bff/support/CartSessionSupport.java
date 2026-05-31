package com.ecommerce.bff.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CartSessionSupport {

    public static final String CART_SESSION_COOKIE = "cart-session";
    public static final String GUEST_SESSION_HEADER = "X-Guest-Session-Id";

    private final int cookieMaxAgeSeconds;

    public CartSessionSupport(@Value("${ecommerce.cart.guest-ttl-hours:24}") long guestTtlHours) {
        this.cookieMaxAgeSeconds = (int) (guestTtlHours * 3600);
    }

    public String resolveGuestSessionId(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = readCookie(request, CART_SESSION_COOKIE);
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(CART_SESSION_COOKIE, sessionId);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(cookieMaxAgeSeconds);
            response.addCookie(cookie);
        }
        return sessionId;
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
