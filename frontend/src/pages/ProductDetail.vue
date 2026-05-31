<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import AppLayout from '@/components/AppLayout.vue';
import { getProductById, type ProductDetail } from '@/services/catalogService';
import { getCartErrorMessage } from '@/services/cartService';
import { useCartStore } from '@/stores/cart';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const product = ref<ProductDetail | null>(null);
const loading = ref(true);
const adding = ref(false);
const error = ref('');
const quantity = ref(1);

const productId = computed(() => Number(route.params.id));

const formatPrice = (price: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(price);

onMounted(async () => {
  if (Number.isNaN(productId.value)) {
    error.value = '无效的商品 ID';
    loading.value = false;
    return;
  }
  try {
    product.value = await getProductById(productId.value);
  } catch {
    error.value = '商品不存在或已下架';
  } finally {
    loading.value = false;
  }
});

function goBack() {
  router.back();
}

async function addToCart() {
  if (!product.value?.inStock) {
    return;
  }
  adding.value = true;
  try {
    await cartStore.addItem(productId.value, quantity.value);
    ElMessage.success('已加入购物车');
  } catch (err) {
    ElMessage.error(getCartErrorMessage(err));
  } finally {
    adding.value = false;
  }
}
</script>

<template>
  <AppLayout />
  <div class="page">
    <el-button link type="primary" @click="goBack">← 返回</el-button>

    <div v-if="loading" class="state">加载中...</div>
    <el-alert v-else-if="error" :title="error" type="error" show-icon :closable="false" />
    <article v-else-if="product" class="detail">
      <div class="gallery">
        <img :src="product.primaryImageUrl" :alt="product.name" />
      </div>
      <div class="info">
        <p v-if="product.brandName" class="brand">{{ product.brandName }}</p>
        <h1>{{ product.name }}</h1>
        <p class="price">{{ formatPrice(product.price) }}</p>
        <p class="category">分类：{{ product.categoryName }}</p>
        <el-tag :type="product.inStock ? 'success' : 'info'">
          {{ product.inStock ? `有货（库存 ${product.stockQuantity}）` : '暂时缺货' }}
        </el-tag>
        <div v-if="product.inStock" class="purchase">
          <el-input-number v-model="quantity" :min="1" :max="product.stockQuantity" />
          <el-button type="primary" :loading="adding" @click="addToCart">加入购物车</el-button>
        </div>
        <div class="description">
          <h3>商品描述</h3>
          <p>{{ product.description || '暂无描述' }}</p>
        </div>
      </div>
    </article>
  </div>
</template>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.state {
  padding: 48px;
  text-align: center;
  color: #909399;
}

.detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  margin-top: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.gallery img {
  width: 100%;
  border-radius: 8px;
  aspect-ratio: 1;
  object-fit: cover;
  background: #f0f2f5;
}

.brand {
  margin: 0 0 4px;
  color: #909399;
  font-size: 13px;
}

.info h1 {
  margin: 0 0 12px;
  font-size: 24px;
}

.price {
  margin: 0 0 12px;
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.category {
  margin: 0 0 12px;
  color: #606266;
  font-size: 14px;
}

.purchase {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0;
}

.description {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.description h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.description p {
  margin: 0;
  line-height: 1.7;
  color: #606266;
}

@media (max-width: 768px) {
  .detail {
    grid-template-columns: 1fr;
  }
}
</style>
