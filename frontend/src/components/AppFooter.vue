<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <!-- Brand -->
      <div class="footer-col brand-col">
        <div class="footer-logo">
          <span class="logo-icon">🛒</span>
          <span>优选商城</span>
        </div>
        <p class="footer-desc">品质生活，从优选开始。<br/>我们精选全球好物，为您带来最优质的购物体验。</p>
        <div class="footer-trust">
          <span>✅ 正品保障</span>
          <span>🔄 7天退换</span>
          <span>🚚 极速配送</span>
          <span>🔒 安全支付</span>
        </div>
      </div>

      <!-- Links -->
      <div class="footer-col">
        <h4>购物指南</h4>
        <RouterLink to="/search">全部商品</RouterLink>
        <a href="#">新品上市</a>
        <a href="#">限时特惠</a>
        <a href="#">品牌专区</a>
      </div>

      <div class="footer-col">
        <h4>客户服务</h4>
        <a href="#">帮助中心</a>
        <a href="#">退换货政策</a>
        <a href="#">配送说明</a>
        <a href="#">投诉建议</a>
      </div>

      <div class="footer-col">
        <h4>关于我们</h4>
        <a href="#">公司介绍</a>
        <a href="#">加入我们</a>
        <a href="#">联系我们</a>
        <a href="#">隐私政策</a>
      </div>

      <!-- Contact -->
      <div class="footer-col contact-col">
        <h4>联系方式</h4>
        <p class="contact-phone">📞 400-888-8888</p>
        <p class="contact-time">周一至周日 9:00-21:00</p>
        <p class="contact-email">✉️ support@youxuanshop.com</p>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="footer-inner">
        <p>© 2026 优选商城 YouXuanShop — 用心优选，品质生活</p>
        <p class="footer-icp">ICP备XXXXXXXX号-1 | 增值电信业务经营许可证</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--brand-secondary);
  color: #b2bec3;
  margin-top: var(--space-4xl);
}

.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-4xl) var(--space-2xl) var(--space-3xl);
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1.3fr;
  gap: var(--space-3xl);
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: #fff;
  margin-bottom: var(--space-md);
}

.footer-desc {
  font-size: var(--font-size-sm);
  line-height: 1.8;
  margin: 0 0 var(--space-lg);
  color: #636e72;
}

.footer-trust {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-sm);
  font-size: var(--font-size-xs);
}

.footer-trust span {
  color: #636e72;
}

.footer-col h4 {
  color: #dfe6e9;
  font-size: var(--font-size-base);
  font-weight: 600;
  margin: 0 0 var(--space-lg);
}

.footer-col a {
  display: block;
  color: #636e72;
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-sm);
  transition: color var(--transition-fast);
}

.footer-col a:hover {
  color: var(--brand-primary);
}

.contact-phone {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: #dfe6e9;
  margin: 0 0 var(--space-sm);
}

.contact-time,
.contact-email {
  font-size: var(--font-size-sm);
  color: #636e72;
  margin: 0 0 var(--space-xs);
}

.footer-bottom {
  border-top: 1px solid rgba(255,255,255,0.06);
  padding: var(--space-xl) 0;
  text-align: center;
}

.footer-bottom .footer-inner {
  display: block;
  padding: 0 var(--space-2xl);
}

.footer-bottom p {
  margin: 0 0 var(--space-xs);
  font-size: var(--font-size-xs);
  color: #636e72;
}

@media (max-width: 768px) {
  .footer-inner {
    grid-template-columns: 1fr 1fr;
    gap: var(--space-2xl);
  }
  .brand-col { grid-column: 1 / -1; }
}
</style>
