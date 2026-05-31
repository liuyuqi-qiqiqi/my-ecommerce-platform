<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import ProductCard from '@/components/ProductCard.vue';
import { getFeaturedProducts, type ProductSummary } from '@/services/catalogService';

const products = ref<ProductSummary[]>([]);
const loading = ref(true);
const error = ref('');

onMounted(async () => {
  try {
    products.value = await getFeaturedProducts();
  } catch {
    error.value = '加载精选商品失败，请稍后重试';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <AppLayout />
  <div class="page">
    <section class="hero">
      <h1>发现精选好物</h1>
      <p>浏览热门商品，开启购物之旅</p>
      <RouterLink to="/search">
        <el-button type="primary">搜索商品</el-button>
      </RouterLink>
    </section>

    <section class="featured">
      <h2>精选商品</h2>
      <div v-if="loading" class="state">加载中...</div>
      <el-alert v-else-if="error" :title="error" type="error" show-icon :closable="false" />
      <div v-else-if="products.length === 0" class="state empty">
        <p>暂无精选商品</p>
        <RouterLink to="/search">浏览全部商品</RouterLink>
      </div>
      <div v-else class="grid">
        <ProductCard v-for="product in products" :key="product.id" :product="product" />
      </div>
    </section>
  </div>
</template>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.hero {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  color: #fff;
  border-radius: 12px;
  padding: 48px 32px;
  margin-bottom: 32px;
}

.hero h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

.hero p {
  margin: 0 0 20px;
  opacity: 0.9;
}

.featured h2 {
  margin: 0 0 20px;
  font-size: 20px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.state {
  text-align: center;
  padding: 48px;
  color: #909399;
}

.empty a {
  color: #409eff;
}
</style>
