<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import ProductCard from '@/components/ProductCard.vue';
import { getFeaturedProducts, type ProductSummary } from '@/services/catalogService';

const router = useRouter();
const products = ref<ProductSummary[]>([]);
const loading = ref(true);
const error = ref('');
const heroBtnLoading = ref(false);

function goSearch() {
  heroBtnLoading.value = true;
  router.push('/search');
}
function goCategory(catId: number) {
  router.push({ path: '/search', query: { categoryId: catId } });
}

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
  <AppLayout>
    <!-- ═══ Hero Banner ═══ -->
    <section class="hero">
      <div class="hero-bg" />
      <div class="hero-content">
        <div class="hero-tag">🏆 品质生活甄选平台</div>
        <h1 class="hero-title">发现你的<span class="highlight">理想好物</span></h1>
        <p class="hero-subtitle">
          精选全球优质品牌，数万好物每日上新<br/>
          <strong>新用户首单享 9 折</strong> · 满 ¥299 全国包邮
        </p>
        <div class="hero-actions">
          <button class="hero-btn primary" :class="{ loading: heroBtnLoading }" @click="goSearch">
            立即探索
            <span class="arrow">→</span>
          </button>
          <RouterLink to="/search" class="hero-btn ghost">查看新品</RouterLink>
        </div>
        <div class="hero-stats">
          <div class="stat"><strong>10,000+</strong><span>精选商品</span></div>
          <div class="stat"><strong>50万+</strong><span>信赖用户</span></div>
          <div class="stat"><strong>99.7%</strong><span>好评率</span></div>
          <div class="stat"><strong>24h</strong><span>极速发货</span></div>
        </div>
      </div>
    </section>

    <!-- ═══ Quick Categories ═══ -->
    <section class="section">
      <h2 class="section-title">热门分类</h2>
      <div class="category-grid">
        <div class="cat-card" @click="goCategory(7)">
          <span class="cat-icon">📱</span>
          <span>手机通讯</span>
        </div>
        <div class="cat-card" @click="goCategory(8)">
          <span class="cat-icon">💻</span>
          <span>电脑办公</span>
        </div>
        <div class="cat-card" @click="goCategory(12)">
          <span class="cat-icon">👔</span>
          <span>男装</span>
        </div>
        <div class="cat-card" @click="goCategory(16)">
          <span class="cat-icon">💄</span>
          <span>面部护理</span>
        </div>
        <div class="cat-card" @click="goCategory(10)">
          <span class="cat-icon">🍳</span>
          <span>厨房用品</span>
        </div>
        <div class="cat-card" @click="goCategory(14)">
          <span class="cat-icon">🍪</span>
          <span>休闲零食</span>
        </div>
        <div class="cat-card" @click="goCategory(18)">
          <span class="cat-icon">⛺</span>
          <span>户外装备</span>
        </div>
        <div class="cat-card" @click="goCategory(9)">
          <span class="cat-icon">🎧</span>
          <span>影音娱乐</span>
        </div>
      </div>
    </section>

    <!-- ═══ Featured Products ═══ -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">为你精选</h2>
        <RouterLink to="/search" class="view-all">查看全部 →</RouterLink>
      </div>

      <div v-if="loading" class="grid">
        <ProductCard v-for="n in 8" :key="n" :product="{} as any" skeleton />
      </div>
      <el-alert
        v-else-if="error"
        :title="error" type="error" show-icon :closable="false"
        style="margin: 32px 0"
      />
      <div v-else-if="products.length === 0" class="empty-featured">
        <p>商品数据加载中，请稍后刷新页面</p>
        <RouterLink to="/search">浏览全部商品 →</RouterLink>
      </div>
      <div v-else class="grid">
        <ProductCard v-for="product in products" :key="product.id" :product="product" />
      </div>
    </section>
  </AppLayout>
</template>

<style scoped>
/* ── Hero ── */
.hero {
  position: relative;
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin: var(--space-2xl) auto var(--space-4xl);
  max-width: var(--max-width);
  margin-left: var(--space-2xl);
  margin-right: var(--space-2xl);
  min-height: 420px;
  display: flex;
  align-items: center;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(45,52,54,0.88) 0%, rgba(45,52,54,0.65) 50%, rgba(255,71,87,0.45) 100%),
    url('https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=1400&h=600&fit=crop') center/cover no-repeat;
  z-index: 0;
}

.hero-content {
  position: relative;
  z-index: 1;
  padding: var(--space-4xl) var(--space-3xl);
  max-width: 580px;
  color: #fff;
}

.hero-tag {
  display: inline-block;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(8px);
  padding: 4px 14px;
  border-radius: 20px;
  font-size: var(--font-size-xs);
  font-weight: 600;
  margin-bottom: var(--space-xl);
  letter-spacing: 1px;
}

.hero-title {
  font-size: var(--font-size-hero);
  font-weight: 800;
  line-height: 1.2;
  margin: 0 0 var(--space-lg);
  letter-spacing: -0.5px;
}

.hero-title .highlight {
  background: linear-gradient(90deg, var(--brand-accent), #ffcc00);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: var(--font-size-lg);
  line-height: 1.8;
  opacity: 0.9;
  margin: 0 0 var(--space-2xl);
}

.hero-subtitle strong {
  color: var(--brand-accent);
}

.hero-actions {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-3xl);
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: 28px;
  font-size: var(--font-size-lg);
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: all var(--transition-smooth);
  text-decoration: none;
}

.hero-btn.primary {
  background: var(--brand-primary);
  color: #fff;
  box-shadow: 0 4px 15px rgba(255,71,87,0.4);
}

.hero-btn.primary:hover {
  background: var(--brand-primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255,71,87,0.5);
}

.hero-btn.primary.loading {
  opacity: 0.7;
  pointer-events: none;
}

.hero-btn .arrow {
  transition: transform var(--transition-fast);
}

.hero-btn.primary:hover .arrow {
  transform: translateX(4px);
}

.hero-btn.ghost {
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(8px);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.25);
}

.hero-btn.ghost:hover {
  background: rgba(255,255,255,0.25);
  transform: translateY(-2px);
}

.hero-stats {
  display: flex;
  gap: var(--space-3xl);
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat strong {
  font-size: var(--font-size-xl);
  font-weight: 700;
}

.stat span {
  font-size: var(--font-size-xs);
  opacity: 0.7;
}

/* ── Sections ── */
.section {
  max-width: var(--max-width);
  margin: 0 auto var(--space-4xl);
  padding: 0 var(--space-2xl);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-xl);
}

.section-header .section-title {
  margin-bottom: 0;
}

.view-all {
  color: var(--brand-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
  transition: transform var(--transition-fast);
}

.view-all:hover {
  transform: translateX(4px);
}

/* ── Category Grid ── */
.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-md);
}

.cat-card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  padding: var(--space-xl);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-smooth);
  box-shadow: var(--shadow-card);
  font-size: var(--font-size-base);
  font-weight: 500;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
}

.cat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover);
  color: var(--brand-primary);
}

.cat-icon {
  font-size: 32px;
}

/* ── Product Grid ── */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-xl);
}

/* ── Empty ── */
.empty-featured {
  text-align: center;
  padding: var(--space-4xl);
  color: var(--text-secondary);
  background: var(--bg-white);
  border-radius: var(--radius-lg);
}

.empty-featured a {
  color: var(--brand-primary);
  font-weight: 600;
}

@media (max-width: 768px) {
  .hero {
    margin-left: var(--space-lg);
    margin-right: var(--space-lg);
    min-height: auto;
  }
  .hero-content { padding: var(--space-3xl) var(--space-xl); }
  .hero-title { font-size: 28px; }
  .hero-stats { flex-wrap: wrap; gap: var(--space-lg); }
  .category-grid { grid-template-columns: repeat(4, 1fr); }
  .grid { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); }
  .cat-icon { font-size: 24px; }
}
</style>
