<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import OrderListItem from '@/components/OrderListItem.vue';
import { getOrderErrorMessage, listOrders, type OrderSummary } from '@/services/orderService';

const orders = ref<OrderSummary[]>([]);
const loading = ref(true);
const error = ref('');
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

onMounted(async () => {
  await loadOrders();
});

async function loadOrders() {
  loading.value = true;
  error.value = '';
  try {
    const result = await listOrders(page.value, pageSize.value);
    orders.value = result.items;
    page.value = result.page;
    pageSize.value = result.pageSize;
    total.value = result.total;
  } catch (err) {
    error.value = getOrderErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

async function onPageChange(nextPage: number) {
  page.value = nextPage;
  await loadOrders();
}
</script>

<template>
  <AppLayout />
  <div class="page">
    <h1>我的订单</h1>

    <div v-if="loading" class="state">加载中...</div>
    <el-alert v-else-if="error" :title="error" type="error" show-icon :closable="false" class="msg" />

    <div v-else-if="!orders.length" class="state empty">
      <el-empty description="暂无订单">
        <RouterLink to="/">
          <el-button type="primary">去首页逛逛</el-button>
        </RouterLink>
      </el-empty>
    </div>

    <template v-else>
      <div class="list">
        <OrderListItem v-for="order in orders" :key="order.id" :order="order" />
      </div>
      <div v-if="total > pageSize" class="pager">
        <el-pagination
          layout="prev, pager, next"
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          @current-change="onPageChange"
        />
      </div>
    </template>
  </div>
</template>

<style scoped>
.page {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.page h1 {
  margin: 0 0 20px;
  font-size: 22px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.state {
  text-align: center;
  padding: 48px;
  color: #909399;
}

.empty {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.msg {
  margin-bottom: 16px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
