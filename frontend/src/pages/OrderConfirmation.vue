<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import { getOrder, getOrderErrorMessage, type OrderDetail } from '@/services/orderService';

const route = useRoute();
const order = ref<OrderDetail | null>(null);
const loading = ref(true);
const error = ref('');

const formatPrice = (price: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(price);

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
    <div class="card">
      <div v-if="loading" class="state">加载中...</div>
      <el-alert v-else-if="error" :title="error" type="error" show-icon :closable="false" />
      <template v-else-if="order">
        <el-result icon="success" title="下单成功" :sub-title="`订单号：${order.orderNumber}`">
          <template #extra>
            <p class="amount">应付总额：{{ formatPrice(order.totalAmount) }}</p>
            <div class="actions">
              <RouterLink :to="`/orders/${order.id}`">
                <el-button type="primary">查看订单详情</el-button>
              </RouterLink>
              <RouterLink to="/">
                <el-button>继续购物</el-button>
              </RouterLink>
            </div>
          </template>
        </el-result>
      </template>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 720px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.amount {
  margin: 0 0 16px;
  font-size: 18px;
  color: #303133;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.state {
  text-align: center;
  padding: 48px;
  color: #909399;
}
</style>
