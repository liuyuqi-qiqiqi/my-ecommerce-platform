package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderDetailDto;
import com.ecommerce.order.dto.OrderPageDto;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderQueryService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    public OrderPageDto listOrders(Long userId, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 50);
        var result = orderRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(safePage - 1, safePageSize));
        var items = result.getContent().stream().map(orderMapper::toSummary).toList();
        return new OrderPageDto(items, safePage, safePageSize, result.getTotalElements());
    }

    public OrderDetailDto getOrder(Long userId, Long orderId) {
        return orderRepository
                .findDetailedByIdAndUserId(orderId, userId)
                .map(orderMapper::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}
