<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import ProductCard from '@/components/ProductCard.vue';
import ProductFilter from '@/components/ProductFilter.vue';
import {
  getCatalogFilters,
  searchProducts,
  type CatalogFilterMeta,
  type ProductSummary,
} from '@/services/catalogService';

const route = useRoute();
const router = useRouter();

const keyword = ref('');
const sortBy = ref<'default' | 'price-asc' | 'price-desc'>('default');
const filters = reactive<{
  categoryId?: number;
  brandId?: number;
  minPrice?: number;
  maxPrice?: number;
}>({});
const meta = ref<CatalogFilterMeta | null>(null);
const products = ref<ProductSummary[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(12);
const loading = ref(false);
const searched = ref(false);

function syncFromRoute() {
  keyword.value = (route.query.q as string) ?? '';
  sortBy.value = (route.query.sort as any) ?? 'default';
  filters.categoryId = route.query.categoryId ? Number(route.query.categoryId) : undefined;
  filters.brandId = route.query.brandId ? Number(route.query.brandId) : undefined;
  filters.minPrice = route.query.minPrice ? Number(route.query.minPrice) : undefined;
  filters.maxPrice = route.query.maxPrice ? Number(route.query.maxPrice) : undefined;
  page.value = route.query.page ? Number(route.query.page) : 1;
}

function sortProducts(list: ProductSummary[]) {
  if (sortBy.value === 'price-asc') return [...list].sort((a, b) => a.price - b.price);
  if (sortBy.value === 'price-desc') return [...list].sort((a, b) => b.price - a.price);
  return list;
}

async function loadResults() {
  loading.value = true;
  searched.value = true;
  try {
    const result = await searchProducts({
      q: keyword.value || undefined,
      categoryId: filters.categoryId,
      brandId: filters.brandId,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      page: page.value,
      pageSize: pageSize.value,
    });
    products.value = sortProducts(result.items);
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

function submitSearch() {
  page.value = 1;
  router.push({
    path: '/search',
    query: {
      ...(keyword.value ? { q: keyword.value } : {}),
      ...(sortBy.value !== 'default' ? { sort: sortBy.value } : {}),
      ...(filters.categoryId ? { categoryId: filters.categoryId } : {}),
      ...(filters.brandId ? { brandId: filters.brandId } : {}),
      ...(filters.minPrice != null ? { minPrice: filters.minPrice } : {}),
      ...(filters.maxPrice != null ? { maxPrice: filters.maxPrice } : {}),
    },
  });
}

function onSortChange(val: string) {
  sortBy.value = val as any;
  submitSearch();
}

function onPageChange(newPage: number) {
  router.push({
    path: '/search',
    query: { ...route.query, page: newPage },
  });
}

onMounted(async () => {
  meta.value = await getCatalogFilters();
  syncFromRoute();
  await loadResults();
});

watch(() => route.query, async () => {
  syncFromRoute();
  await loadResults();
});
</script>

<template>
  <AppLayout>
    <div class="page">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索商品关键词"
          clearable
          @keyup.enter="submitSearch"
        >
          <template #append>
            <el-button type="primary" @click="submitSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div class="layout">
        <ProductFilter v-model="filters" :meta="meta" @apply="submitSearch" />

        <main class="results">
          <div v-if="!loading && searched" class="results-bar">
            <p class="result-count">共 {{ total }} 件商品</p>
            <el-select v-model="sortBy" size="small" style="width:140px" @change="onSortChange" placeholder="排序">
              <el-option label="默认排序" value="default" />
              <el-option label="价格从低到高" value="price-asc" />
              <el-option label="价格从高到低" value="price-desc" />
            </el-select>
          </div>

          <div v-if="loading" class="grid">
            <ProductCard v-for="n in 6" :key="n" :product="{} as any" skeleton />
          </div>
          <div v-else-if="searched && products.length === 0" class="state empty">
            <el-empty description="没有找到匹配的商品">
              <template #default>
                <p class="hint">试试放宽筛选条件，或浏览其他分类</p>
                <RouterLink to="/">
                  <el-button type="primary">返回首页</el-button>
                </RouterLink>
              </template>
            </el-empty>
          </div>
          <template v-else>
            <div class="grid">
              <ProductCard v-for="product in products" :key="product.id" :product="product" />
            </div>
            <div v-if="total > pageSize" class="pagination">
              <el-pagination
                :current-page="page"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                @current-change="onPageChange"
              />
            </div>
          </template>
        </main>
      </div>
    </div>
  </AppLayout>
</template>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.search-bar {
  margin-bottom: 20px;
}

.layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 20px;
  align-items: start;
}

.results {
  min-height: 300px;
}

.results-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.result-count {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.state {
  padding: 48px;
  text-align: center;
  color: #909399;
}

.hint {
  margin: 0 0 12px;
  color: #909399;
  font-size: 13px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
