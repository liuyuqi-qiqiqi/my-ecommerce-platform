<script setup lang="ts">
import { RouterLink } from 'vue-router';
import type { ProductSummary } from '@/services/catalogService';

defineProps<{
  product: ProductSummary;
}>();

const formatPrice = (price: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(price);
</script>

<template>
  <RouterLink :to="`/products/${product.id}`" class="product-card">
    <div class="image-wrap">
      <img :src="product.primaryImageUrl" :alt="product.name" loading="lazy" />
      <span v-if="!product.inStock" class="badge out-of-stock">缺货</span>
    </div>
    <div class="info">
      <p v-if="product.brandName" class="brand">{{ product.brandName }}</p>
      <h3 class="name">{{ product.name }}</h3>
      <p class="price">{{ formatPrice(product.price) }}</p>
    </div>
  </RouterLink>
</template>

<style scoped>
.product-card {
  display: block;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  text-decoration: none;
  color: inherit;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.image-wrap {
  position: relative;
  aspect-ratio: 1;
  background: #f0f2f5;
}

.image-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
}

.out-of-stock {
  background: #909399;
}

.info {
  padding: 12px;
}

.brand {
  margin: 0 0 4px;
  font-size: 12px;
  color: #909399;
}

.name {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}
</style>
