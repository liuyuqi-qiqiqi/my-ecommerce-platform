# RabbitMQ Domain Events

**Feature**: 001-ecommerce-platform  
**Format**: JSON message body, `application/json` content-type

## Exchange & Queue Layout

| Exchange | Type | Purpose |
|----------|------|---------|
| `commerce.events` | topic | All domain events |

Routing keys follow `{service}.{event}` pattern.

---

## product-service Events

### ProductCreated / ProductUpdated

```json
{
  "eventId": "uuid",
  "eventType": "ProductCreated",
  "timestamp": "2026-05-31T12:00:00Z",
  "payload": {
    "productId": 1,
    "sku": "SKU-001",
    "name": "Example Product",
    "price": 99.99,
    "brandId": 1,
    "brandName": "BrandA",
    "categoryId": 2,
    "categoryName": "Electronics",
    "stockQuantity": 100,
    "featured": true,
    "status": "ACTIVE"
  }
}
```

### StockChanged

Published after reservation, release, or admin adjustment.

```json
{
  "eventId": "uuid",
  "eventType": "StockChanged",
  "timestamp": "2026-05-31T12:00:00Z",
  "payload": {
    "productId": 1,
    "stockQuantity": 98,
    "changeReason": "ORDER_RESERVATION"
  }
}
```

---

## order-service Events

### OrderCreated

Routing key: `order.OrderCreated`  
Consumer: product-service (stock reservation)

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "timestamp": "2026-05-31T12:00:00Z",
  "payload": {
    "orderId": 100,
    "orderNumber": "ORD-20260531-000100",
    "userId": 42,
    "items": [
      { "productId": 1, "quantity": 2, "unitPrice": 99.99 }
    ]
  }
}
```

### OrderConfirmed

Routing key: `order.OrderConfirmed`  
Consumer: cart-service (clear cart)

```json
{
  "eventId": "uuid",
  "eventType": "OrderConfirmed",
  "timestamp": "2026-05-31T12:00:01Z",
  "payload": {
    "orderId": 100,
    "orderNumber": "ORD-20260531-000100",
    "userId": 42
  }
}
```

### OrderCancelled

Routing key: `order.OrderCancelled`  
Consumer: product-service (release stock compensation)

```json
{
  "eventId": "uuid",
  "eventType": "OrderCancelled",
  "timestamp": "2026-05-31T12:00:01Z",
  "payload": {
    "orderId": 100,
    "reason": "STOCK_RESERVATION_FAILED",
    "items": [
      { "productId": 1, "quantity": 2 }
    ]
  }
}
```

---

## product-service Responses

### StockReserved

Routing key: `product.StockReserved`  
Consumer: order-service

```json
{
  "eventId": "uuid",
  "eventType": "StockReserved",
  "timestamp": "2026-05-31T12:00:01Z",
  "payload": {
    "orderId": 100,
    "items": [
      { "productId": 1, "quantity": 2, "reserved": true }
    ]
  }
}
```

### StockReservationFailed

```json
{
  "eventId": "uuid",
  "eventType": "StockReservationFailed",
  "timestamp": "2026-05-31T12:00:01Z",
  "payload": {
    "orderId": 100,
    "failedItems": [
      { "productId": 1, "requestedQuantity": 2, "availableQuantity": 0 }
    ]
  }
}
```

---

## Reliability Requirements

- Publishers MUST use publisher confirms
- Consumers MUST use manual ACK with idempotent handlers keyed by `eventId`
- Failed messages route to DLQ `commerce.events.dlq` after 3 retries with exponential backoff
