<script setup lang="ts">
import { RouterLink } from 'vue-router';
import {
  formatOrderDate,
  formatPrice,
  getOrderStatusLabel,
  type OrderSummary,
} from '@/services/orderService';

defineProps<{
  order: OrderSummary;
}>();

const statusType = (status: string) => {
  switch (status) {
    case 'DELIVERED':
      return 'success';
    case 'SHIPPED':
      return 'primary';
    case 'CANCELLED':
      return 'info';
    case 'PENDING':
      return 'warning';
    default:
      return '';
  }
};
</script>

<template>
  <RouterLink :to="`/orders/${order.id}`" class="order-item">
    <div class="main">
      <div class="head">
        <span class="number">订单号 {{ order.orderNumber }}</span>
        <el-tag :type="statusType(order.status)" size="small">
          {{ getOrderStatusLabel(order.status) }}
        </el-tag>
      </div>
      <p class="date">{{ formatOrderDate(order.createdAt) }}</p>
    </div>
    <div class="total">{{ formatPrice(order.totalAmount) }}</div>
  </RouterLink>
</template>

<style scoped>
.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.order-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.12);
}

.head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.number {
  font-weight: 600;
  color: #303133;
}

.date {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.total {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}
</style>
