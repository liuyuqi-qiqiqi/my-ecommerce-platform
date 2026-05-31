package com.ecommerce.cart.event;

import com.ecommerce.cart.service.CartService;
import com.ecommerce.cart.service.IdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConfirmedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedListener.class);

    private final IdempotencyService idempotencyService;
    private final CartService cartService;

    public OrderConfirmedListener(IdempotencyService idempotencyService, CartService cartService) {
        this.idempotencyService = idempotencyService;
        this.cartService = cartService;
    }

    @RabbitListener(queues = "${ecommerce.rabbitmq.order-confirmed-queue:cart.order-confirmed}")
    public void onOrderConfirmed(CommerceEvent event) {
        if (!idempotencyService.registerIfNew(event.eventId())) {
            return;
        }
        Long userId = Long.valueOf(event.payload().get("userId").toString());
        cartService.clearUserCart(userId);
        log.info("Cart cleared after order confirmation for userId={}", userId);
    }
}
