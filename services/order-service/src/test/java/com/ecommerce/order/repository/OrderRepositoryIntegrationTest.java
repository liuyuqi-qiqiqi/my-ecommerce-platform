package com.ecommerce.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("order_db")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void listOrdersByUserNewestFirst() {
        Order older = buildOrder("ORD-OLD", Instant.now().minusSeconds(3600));
        Order newer = buildOrder("ORD-NEW", Instant.now());
        orderRepository.save(older);
        orderRepository.save(newer);

        var page = orderRepository.findByUserIdOrderByCreatedAtDesc(7L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getOrderNumber()).isEqualTo("ORD-NEW");
    }

    private Order buildOrder(String orderNumber, Instant createdAt) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(7L);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setSubtotal(BigDecimal.TEN);
        order.setShippingFee(BigDecimal.ONE);
        order.setTotalAmount(BigDecimal.valueOf(11));
        order.setPaymentMethod("PAY_ON_DELIVERY");
        order.setAddressSnapshot("{\"recipientName\":\"Test\"}");
        order.setCreatedAt(createdAt);
        order.setUpdatedAt(createdAt);
        return order;
    }
}
