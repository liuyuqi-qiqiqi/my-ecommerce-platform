package com.ecommerce.order.exception;

import java.util.List;
import java.util.Map;

public class StockConflictException extends RuntimeException {

    private final List<Map<String, Object>> failedItems;

    public StockConflictException(List<Map<String, Object>> failedItems) {
        super("部分商品库存不足，请返回购物车调整");
        this.failedItems = failedItems;
    }

    public List<Map<String, Object>> getFailedItems() {
        return failedItems;
    }
}
