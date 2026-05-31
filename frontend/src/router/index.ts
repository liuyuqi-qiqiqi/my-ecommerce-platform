import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/pages/Home.vue'),
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/pages/Search.vue'),
    },
    {
      path: '/products/:id',
      name: 'product-detail',
      component: () => import('@/pages/ProductDetail.vue'),
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/pages/Cart.vue'),
    },
    {
      path: '/checkout',
      name: 'checkout',
      meta: { requiresAuth: true },
      component: () => import('@/pages/Checkout.vue'),
    },
    {
      path: '/orders',
      name: 'orders',
      meta: { requiresAuth: true },
      component: () => import('@/pages/Orders.vue'),
    },
    {
      path: '/orders/:id/confirmation',
      name: 'order-confirmation',
      meta: { requiresAuth: true },
      component: () => import('@/pages/OrderConfirmation.vue'),
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      meta: { requiresAuth: true },
      component: () => import('@/pages/OrderDetail.vue'),
    },
    {
      path: '/login',
      name: 'login',
      meta: { guestOnly: true },
      component: () => import('@/pages/Login.vue'),
    },
    {
      path: '/register',
      name: 'register',
      meta: { guestOnly: true },
      component: () => import('@/pages/Register.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      meta: { requiresAuth: true },
      component: () => import('@/pages/Profile.vue'),
    },
  ],
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  if (!authStore.initialized) {
    authStore.hydrateFromStorage();
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'home' };
  }

  return true;
});

export default router;
