<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import ShipmentTracker from '@/components/ShipmentTracker.vue';
import {
  formatOrderDate,
  formatPrice,
  getOrder,
  getOrderErrorMessage,
  getOrderStatusLabel,
  getPaymentMethodLabel,
  type OrderDetail,
} from '@/services/orderService';

const route = useRoute();
const order = ref<OrderDetail | null>(null);
const loading = ref(true);
const error = ref('');

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

onMounted(async () => {
  const orderId = Number(route.params.id);
  if (!orderId) {
    error.value = '无效的订单';
    loading.value = false;
    return;
  }
  try {
    order.value = await getOrder(orderId);
  } catch (err) {
    error.value = getOrderErrorMessage(err);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <AppLayout />
  <div class="page">
    <div class="back">
      <RouterLink to="/orders">← 返回订单列表</RouterLink>
    </div>

    <div v-if="loading" class="state">加载中...</div>
    <el-alert v-else-if="error" :title="error" type="error" show-icon :closable="false" />

    <template v-else-if="order">
      <header class="head card">
        <div>
          <h1>订单详情</h1>
          <p class="meta">订单号 {{ order.orderNumber }} · {{ formatOrderDate(order.createdAt) }}</p>
        </div>
        <el-tag :type="statusType(order.status)" size="large">
          {{ getOrderStatusLabel(order.status) }}
        </el-tag>
      </header>

      <div class="layout">
        <section class="card">
          <h2>商品清单</h2>
          <div v-for="item in order.items" :key="item.productId" class="line">
            <div>
              <strong>{{ item.productName }}</strong>
              <p class="sub">× {{ item.quantity }}</p>
            </div>
            <span>{{ formatPrice(item.lineSubtotal) }}</span>
          </div>
        </section>

        <aside class="side">
          <section class="card">
            <h2>收货地址</h2>
            <p class="address">
              <strong>{{ order.address.recipientName }}</strong>
              {{ order.address.phone }}
            </p>
            <p class="address">
              {{ order.address.province }}{{ order.address.city }}{{ order.address.district
              }}{{ order.address.street }}（{{ order.address.postalCode }}）
            </p>
          </section>

          <section class="card">
            <h2>费用明细</h2>
            <div class="row">
              <span>商品小计</span>
              <span>{{ formatPrice(order.subtotal) }}</span>
            </div>
            <div class="row">
              <span>运费</span>
              <span>{{ formatPrice(order.shippingFee) }}</span>
            </div>
            <div class="row">
              <span>支付方式</span>
              <span>{{ getPaymentMethodLabel(order.paymentMethod) }}</span>
            </div>
            <div class="row total">
              <span>应付总额</span>
              <span>{{ formatPrice(order.totalAmount) }}</span>
            </div>
          </section>

          <section class="card">
            <ShipmentTracker :shipment="order.shipment" />
          </section>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.back {
  margin-bottom: 16px;
}

.back a {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.head h1 {
  margin: 0 0 6px;
  font-size: 22px;
}

.meta {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;
  align-items: start;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card h2 {
  margin: 0 0 16px;
  font-size: 16px;
}

.side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.line {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.line:last-child {
  border-bottom: none;
}

.sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}

.address {
  margin: 0 0 8px;
  color: #606266;
  line-height: 1.6;
}

.row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.row.total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.state {
  text-align: center;
  padding: 48px;
  color: #909399;
}

@media (max-width: 768px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
