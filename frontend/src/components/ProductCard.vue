<script setup lang="ts">
import { computed, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { ProductSummary } from '@/services/catalogService';
import { useCartStore } from '@/stores/cart';
import { getCartErrorMessage } from '@/services/cartService';

const props = defineProps<{
  product: ProductSummary;
  skeleton?: boolean;
}>();

const cartStore = useCartStore();
const imgError = ref(false);
const adding = ref(false);

const formatPrice = (price: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(price);

// Mock original price for visual effect (~115% of current)
const originalPrice = computed(() => {
  if (!props.product?.price) return 0;
  return Math.round(props.product.price * 1.18 * 100) / 100;
});

const discountPercent = computed(() => {
  if (!props.product?.price || originalPrice.value <= props.product.price) return 0;
  return Math.round((1 - props.product.price / originalPrice.value) * 100);
});

// Sale tag based on product properties
const saleTag = computed(() => {
  if (!props.product) return '';
  if (discountPercent.value >= 15) return '爆款';
  if (discountPercent.value >= 5) return '热卖';
  return '';
});

const fallbackImg = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" fill="%23f0f2f5">' +
  '<rect width="400" height="400"/>' +
  '<text x="200" y="205" text-anchor="middle" fill="%23b2bec3" font-size="52" font-family="system-ui">📷</text>' +
  '<text x="200" y="240" text-anchor="middle" fill="%23909399" font-size="14" font-family="system-ui">暂无图片</text>' +
  '</svg>'
);

async function quickAdd(event: Event) {
  event.preventDefault();
  event.stopPropagation();
  if (!props.product?.inStock || adding.value) return;
  adding.value = true;
  try {
    await cartStore.addItem(props.product.id, 1);
    ElMessage.success('已加入购物车');
  } catch (err) {
    ElMessage.error(getCartErrorMessage(err));
  } finally {
    adding.value = false;
  }
}
</script>

<template>
  <!-- Skeleton -->
  <div v-if="skeleton" class="card skeleton-card">
    <div class="card-img skeleton-bg" />
    <div class="card-body">
      <div class="skeleton-line short" />
      <div class="skeleton-line" />
      <div class="skeleton-line half" />
    </div>
  </div>

  <!-- Real Card -->
  <RouterLink v-else :to="`/products/${product.id}`" class="card">
    <!-- Image -->
    <div class="card-img">
      <img
        :src="imgError ? fallbackImg : product.primaryImageUrl"
        :alt="product.name"
        loading="lazy"
        @error="imgError = true"
      />
      <!-- Tags -->
      <span v-if="saleTag" class="tag sale">{{ saleTag }}</span>
      <span v-else-if="!product.inStock" class="tag oos">缺货</span>
      <span v-if="discountPercent >= 15" class="tag discount">-{{ discountPercent }}%</span>
      <!-- Quick Add -->
      <button
        v-if="product.inStock"
        :class="['quick-add', { adding }]"
        :disabled="adding"
        @click="quickAdd"
        title="加入购物车"
      >
        <span v-if="adding" class="spinner" />
        <span v-else>🛒</span>
      </button>
    </div>

    <!-- Info -->
    <div class="card-body">
      <p v-if="product.brandName" class="brand">{{ product.brandName }}</p>
      <h3 class="name">{{ product.name }}</h3>
      <div class="price-row">
        <span class="price">{{ formatPrice(product.price) }}</span>
        <span v-if="discountPercent >= 5" class="orig-price">{{ formatPrice(originalPrice) }}</span>
      </div>
    </div>
  </RouterLink>
</template>

<style scoped>
/* ── Card Base ── */
.card {
  display: flex;
  flex-direction: column;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-card);
  text-decoration: none;
  color: inherit;
  transition: transform var(--transition-smooth), box-shadow var(--transition-smooth);
  position: relative;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-card-hover);
}

/* ── Image ── */
.card-img {
  position: relative;
  aspect-ratio: 1;
  background: #f5f6f8;
  overflow: hidden;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover .card-img img {
  transform: scale(1.06);
}

/* ── Tags ── */
.tag {
  position: absolute;
  top: 10px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  line-height: 1.5;
  letter-spacing: 0.5px;
}

.tag.sale {
  left: 10px;
  background: linear-gradient(135deg, var(--brand-primary), #ff6348);
}

.tag.oos {
  left: 10px;
  background: #b2bec3;
}

.tag.discount {
  right: 10px;
  background: var(--brand-accent);
}

/* ── Quick Add Button ── */
.quick-add {
  position: absolute;
  right: 10px;
  bottom: 10px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: var(--brand-primary);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(255,71,87,0.35);
  transition: all var(--transition-fast);
  opacity: 0;
  transform: translateY(8px);
}

.card:hover .quick-add {
  opacity: 1;
  transform: translateY(0);
}

.quick-add:hover {
  background: var(--brand-primary-hover);
  transform: scale(1.1) !important;
}

.quick-add:active {
  transform: scale(0.95) !important;
}

.quick-add.adding {
  opacity: 1;
  pointer-events: none;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ── Card Body ── */
.card-body {
  padding: var(--space-lg);
  display: flex;
  flex-direction: column;
  flex: 1;
}

.brand {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin: 0 0 4px;
  letter-spacing: 0.3px;
}

.name {
  font-size: var(--font-size-base);
  font-weight: 600;
  line-height: 1.45;
  margin: 0 0 var(--space-sm);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--text-primary);
  flex: 1;
}

/* ── Price ── */
.price-row {
  display: flex;
  align-items: baseline;
  gap: var(--space-sm);
  margin-top: auto;
}

.price {
  font-size: var(--font-size-xl);
  font-weight: 800;
  color: var(--brand-primary);
  letter-spacing: -0.3px;
}

.orig-price {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  text-decoration: line-through;
}

/* ── Skeleton ── */
.skeleton-card {
  pointer-events: none;
}

.skeleton-bg {
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-line {
  height: 14px;
  border-radius: 4px;
  margin-bottom: 8px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-line.short { width: 40%; }
.skeleton-line.half { width: 60%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
