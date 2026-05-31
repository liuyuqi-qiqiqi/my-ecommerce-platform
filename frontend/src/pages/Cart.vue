<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import CartItem from '@/components/CartItem.vue';
import { getCartErrorMessage } from '@/services/cartService';
import { useAuthStore } from '@/stores/auth';
import { useCartStore } from '@/stores/cart';

const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();

const updatingProductId = ref<number | null>(null);
const error = ref('');

const formatPrice = (price: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(price);

onMounted(async () => {
  await loadCart();
});

async function loadCart() {
  error.value = '';
  try {
    await cartStore.fetchCart();
  } catch (err) {
    error.value = getCartErrorMessage(err);
  }
}

async function updateQuantity(productId: number, quantity: number) {
  updatingProductId.value = productId;
  error.value = '';
  try {
    await cartStore.updateQuantity(productId, quantity);
  } catch (err) {
    error.value = getCartErrorMessage(err);
  } finally {
    updatingProductId.value = null;
  }
}

async function removeItem(productId: number) {
  updatingProductId.value = productId;
  error.value = '';
  try {
    await cartStore.removeItem(productId);
  } catch (err) {
    error.value = getCartErrorMessage(err);
  } finally {
    updatingProductId.value = null;
  }
}

function goCheckout() {
  if (!authStore.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: '/checkout' } });
    return;
  }
  if (!cartStore.canCheckout) {
    return;
  }
  router.push('/checkout');
}
</script>

<template>
  <AppLayout />
  <div class="page">
    <h1>购物车</h1>

    <div v-if="cartStore.loading" class="state">加载中...</div>
    <el-alert v-else-if="error" :title="error" type="error" show-icon :closable="false" class="msg" />

    <div v-else-if="!cartStore.cart?.items.length" class="state empty">
      <el-empty description="购物车是空的">
        <RouterLink to="/search">
          <el-button type="primary">去逛逛</el-button>
        </RouterLink>
      </el-empty>
    </div>

    <div v-else class="layout">
      <div class="items card">
        <CartItem
          v-for="item in cartStore.cart.items"
          :key="item.productId"
          :item="item"
          :updating="updatingProductId === item.productId"
          @update-quantity="(qty) => updateQuantity(item.productId, qty)"
          @remove="removeItem(item.productId)"
        />
      </div>

      <aside class="summary card">
        <h3>订单摘要</h3>
        <p class="row">
          <span>商品件数</span>
          <span>{{ cartStore.itemCount }}</span>
        </p>
        <p class="row total">
          <span>小计</span>
          <span>{{ formatPrice(cartStore.subtotal) }}</span>
        </p>
        <el-alert
          v-if="cartStore.hasUnavailableItems"
          title="部分商品不可用，请调整后再结算"
          type="warning"
          show-icon
          :closable="false"
          class="msg"
        />
        <el-button type="primary" style="width: 100%" :disabled="!cartStore.canCheckout" @click="goCheckout">
          去结算
        </el-button>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.page h1 {
  margin: 0 0 20px;
  font-size: 22px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}

.summary h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.row {
  display: flex;
  justify-content: space-between;
  margin: 0 0 12px;
  color: #606266;
}

.row.total {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.state {
  text-align: center;
  padding: 48px;
  color: #909399;
}

.msg {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
