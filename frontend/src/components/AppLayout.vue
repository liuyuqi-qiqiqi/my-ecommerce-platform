<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useCartStore } from '@/stores/cart';
import AppFooter from '@/components/AppFooter.vue';

const authStore = useAuthStore();
const cartStore = useCartStore();
const router = useRouter();
const scrolled = ref(false);
const promoClosed = ref(false);

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

onMounted(() => {
  window.addEventListener('scroll', () => { scrolled.value = window.scrollY > 20; }, { passive: true });
  try { cartStore.fetchCart(); } catch { /* non-critical */ }
});

async function handleLogout() {
  await authStore.logout();
  cartStore.clearLocal();
  await router.push('/login');
}
</script>

<template>
  <!-- Promo Bar -->
  <div v-if="!promoClosed" class="promo-bar">
    <div class="promo-inner">
      <span class="promo-icon">🔥</span>
      <span class="promo-text">新用户首单享 <strong>9折优惠</strong> · 全场满299包邮 · 品质保证，7天无理由退换</span>
      <span class="promo-close" @click="promoClosed = true">✕</span>
    </div>
  </div>

  <!-- Sticky Nav -->
  <header :class="['app-header', { scrolled }]">
    <div class="nav-inner">
      <RouterLink to="/" class="logo">
        <span class="logo-icon">🛒</span>
        <span class="logo-text">优选商城</span>
      </RouterLink>

      <nav class="nav-links">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/search">全部商品</RouterLink>
        <RouterLink to="/cart" class="cart-link">
          购物车
          <span v-if="cartStore.itemCount > 0" class="cart-badge">{{ cartStore.itemCount }}</span>
        </RouterLink>
      </nav>

      <div class="nav-actions">
        <template v-if="authStore.isAuthenticated">
          <RouterLink to="/orders">我的订单</RouterLink>
          <RouterLink to="/profile">{{ authStore.displayName || '个人中心' }}</RouterLink>
          <a class="nav-link" @click.prevent="handleLogout">退出</a>
        </template>
        <template v-else>
          <RouterLink to="/login">登录</RouterLink>
          <RouterLink to="/register" class="btn-register">免费注册</RouterLink>
        </template>
      </div>
    </div>
  </header>

  <!-- Page content injected here -->
  <div class="app-body">
    <slot />
  </div>

  <!-- Footer -->
  <AppFooter />

  <!-- Back to Top -->
  <Transition name="fade">
    <button v-if="scrolled" class="back-to-top" @click="scrollToTop" title="返回顶部">
      ↑
    </button>
  </Transition>
</template>

<style scoped>
/* ── Promo Bar ── */
.promo-bar {
  background: linear-gradient(90deg, var(--brand-primary), #ff6348);
  color: #fff;
  height: var(--promo-height);
  position: relative;
  z-index: 10;
}
.promo-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-2xl);
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  font-size: var(--font-size-sm);
}
.promo-icon { font-size: 14px; }
.promo-text strong { font-weight: 700; }
.promo-close {
  position: absolute;
  right: var(--space-2xl);
  cursor: pointer;
  opacity: 0.7;
  font-size: var(--font-size-base);
  padding: 2px 6px;
  border-radius: 3px;
  transition: opacity var(--transition-fast);
}
.promo-close:hover { opacity: 1; background: rgba(255,255,255,0.15); }

/* ── Header ── */
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255,255,255,0.97);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid transparent;
  transition: all var(--transition-smooth);
  height: var(--nav-height);
}
.app-header.scrolled {
  box-shadow: var(--shadow-nav);
  border-bottom-color: var(--border-light);
}
.nav-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-2xl);
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--space-3xl);
}
.nav-spacer { height: var(--nav-height); }

/* ── Logo ── */
.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--font-size-xl);
  font-weight: 800;
  color: var(--brand-primary);
  text-decoration: none;
  flex-shrink: 0;
}
.logo-icon { font-size: 24px; }

/* ── Nav Links ── */
.nav-links {
  display: flex;
  gap: var(--space-2xl);
  flex: 1;
}
.nav-links a,
.nav-actions a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: var(--font-size-base);
  font-weight: 500;
  padding: var(--space-xs) 0;
  position: relative;
  transition: color var(--transition-fast);
  white-space: nowrap;
}
.nav-links a::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 100%;
  height: 2px;
  background: var(--brand-primary);
  border-radius: 1px;
  transition: right var(--transition-smooth);
}
.nav-links a:hover,
.nav-links a.router-link-active {
  color: var(--text-primary);
}
.nav-links a.router-link-active::after {
  right: 0;
}
.nav-actions a.router-link-active {
  color: var(--brand-primary);
  font-weight: 600;
}

/* ── Cart Badge ── */
.cart-link { position: relative; }
.cart-badge {
  position: absolute;
  top: -8px;
  right: -14px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--brand-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: badgePop 0.3s ease;
}
@keyframes badgePop {
  0% { transform: scale(0); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

/* ── Nav Actions ── */
.nav-actions {
  display: flex;
  gap: var(--space-lg);
  align-items: center;
  flex-shrink: 0;
}
.nav-link { cursor: pointer; }
.btn-register {
  display: inline-flex;
  align-items: center;
  padding: 6px 18px;
  border-radius: 20px;
  background: var(--brand-primary);
  color: #fff !important;
  font-weight: 600;
  font-size: var(--font-size-sm);
  transition: background var(--transition-fast), transform var(--transition-fast);
}
.btn-register:hover {
  background: var(--brand-primary-hover);
  transform: scale(1.03);
}

/* ── Body ── */
.app-body {
  min-height: calc(100vh - var(--nav-height) - var(--promo-height));
}

/* ── Back to Top ── */
.back-to-top {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 999;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: var(--brand-primary);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: var(--shadow-card-hover);
  transition: transform var(--transition-fast), background var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
}
.back-to-top:hover {
  transform: translateY(-3px);
  background: var(--brand-primary-hover);
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 768px) {
  .nav-links { gap: var(--space-lg); }
  .nav-actions { gap: var(--space-md); }
  .nav-links a,
  .nav-actions a { font-size: var(--font-size-sm); }
}
</style>
