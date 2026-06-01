<script setup lang="ts">
import { RouterView } from 'vue-router';
import ToastContainer from '@/components/ToastContainer.vue';
</script>

<template>
  <RouterView />
  <ToastContainer />
</template>

<style>
/* ============================================================
   E-Commerce Brand Design System
   ============================================================ */

:root {
  /* ── Brand Colors ── */
  --brand-primary: #ff4757;
  --brand-primary-hover: #e8384a;
  --brand-primary-light: #fff0f1;
  --brand-secondary: #2d3436;
  --brand-accent: #ffa502;

  /* ── Neutral Colors ── */
  --bg-page: #f0f2f5;
  --bg-white: #ffffff;
  --text-primary: #1a1a2e;
  --text-secondary: #636e72;
  --text-muted: #b2bec3;
  --border-light: #e8eaed;
  --border-card: #eaecef;

  /* ── Functional Colors ── */
  --color-success: #00b894;
  --color-warning: #fdcb6e;
  --color-error: #ff4757;
  --color-info: #0984e3;

  /* ── Typography ── */
  --font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', system-ui, -apple-system, sans-serif;
  --font-size-xs: 12px;
  --font-size-sm: 13px;
  --font-size-base: 14px;
  --font-size-lg: 16px;
  --font-size-xl: 18px;
  --font-size-2xl: 22px;
  --font-size-3xl: 28px;
  --font-size-hero: 38px;

  /* ── Spacing ── */
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 20px;
  --space-2xl: 24px;
  --space-3xl: 32px;
  --space-4xl: 48px;

  /* ── Radii ── */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;
  --radius-xl: 20px;

  /* ── Shadows ── */
  --shadow-card: 0 2px 8px rgba(0,0,0,0.06);
  --shadow-card-hover: 0 8px 25px rgba(0,0,0,0.10);
  --shadow-nav: 0 1px 3px rgba(0,0,0,0.06);
  --shadow-hero: 0 4px 24px rgba(0,0,0,0.15);

  /* ── Transitions ── */
  --transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-smooth: 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  /* ── Layout ── */
  --max-width: 1240px;
  --nav-height: 60px;
  --promo-height: 36px;
}

/* ── Reset ── */
*,
*::before,
*::after {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: var(--font-family);
  font-size: var(--font-size-base);
  color: var(--text-primary);
  background: var(--bg-page);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  line-height: 1.6;
}

a {
  color: inherit;
  text-decoration: none;
}

img {
  max-width: 100%;
  display: block;
}

button {
  font-family: inherit;
}

/* ── Utility Classes ── */
.container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-2xl);
}

.section-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  margin: 0 0 var(--space-xl);
  position: relative;
  padding-left: var(--space-lg);
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: var(--brand-primary);
  border-radius: 2px;
}

/* ── Scrollbar ── */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--text-muted);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

/* ── Element Plus Overrides ── */
.el-button--primary {
  --el-button-bg-color: var(--brand-primary);
  --el-button-border-color: var(--brand-primary);
  --el-button-hover-bg-color: var(--brand-primary-hover);
  --el-button-hover-border-color: var(--brand-primary-hover);
  --el-button-active-bg-color: var(--brand-primary-hover);
  --el-button-active-border-color: var(--brand-primary-hover);
  border-radius: var(--radius-sm);
  font-weight: 600;
}

.el-pagination {
  --el-pagination-button-bg-color: var(--bg-white);
}
</style>
