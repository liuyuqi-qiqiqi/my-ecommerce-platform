<script setup lang="ts">
import { onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useCartStore } from '@/stores/cart';

const authStore = useAuthStore();
const cartStore = useCartStore();
const router = useRouter();

onMounted(async () => {
  try {
    await cartStore.fetchCart();
  } catch {
    // cart badge optional — ignore errors on layout load
  }
});

async function handleLogout() {
  await authStore.logout();
  cartStore.clearLocal();
  await router.push('/login');
}
</script>

<template>
  <header class="app-header">
    <div class="inner">
      <RouterLink to="/" class="logo">电商商城</RouterLink>
      <nav>
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/search">搜索</RouterLink>
        <RouterLink to="/cart" class="cart-link">
          购物车
          <span v-if="cartStore.itemCount > 0" class="badge">{{ cartStore.itemCount }}</span>
        </RouterLink>
        <template v-if="authStore.isAuthenticated">
          <RouterLink to="/orders">我的订单</RouterLink>
          <RouterLink to="/profile">{{ authStore.displayName || '个人资料' }}</RouterLink>
          <a href="#" class="link" @click.prevent="handleLogout">退出</a>
        </template>
        <template v-else>
          <RouterLink to="/login">登录</RouterLink>
          <RouterLink to="/register">注册</RouterLink>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 24px;
}

.inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  text-decoration: none;
}

nav {
  display: flex;
  gap: 20px;
  align-items: center;
}

nav a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
}

nav a.router-link-active {
  color: #409eff;
  font-weight: 500;
}

.cart-link {
  position: relative;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  margin-left: 4px;
  border-radius: 9px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.link {
  cursor: pointer;
}
</style>
