<script setup lang="ts">
import { reactive, watch } from 'vue';
import type { CatalogFilterMeta } from '@/services/catalogService';

const props = defineProps<{
  meta: CatalogFilterMeta | null;
  modelValue: {
    categoryId?: number;
    brandId?: number;
    minPrice?: number;
    maxPrice?: number;
  };
}>();

const emit = defineEmits<{
  'update:modelValue': [value: typeof props.modelValue];
  apply: [];
}>();

const local = reactive({ ...props.modelValue });

watch(
  () => props.modelValue,
  (value) => Object.assign(local, value),
  { deep: true },
);

function applyFilters() {
  emit('update:modelValue', { ...local });
  emit('apply');
}

function resetFilters() {
  local.categoryId = undefined;
  local.brandId = undefined;
  local.minPrice = undefined;
  local.maxPrice = undefined;
  applyFilters();
}
</script>

<template>
  <aside class="filter-panel">
    <h3>筛选</h3>

    <div class="field">
      <label>分类</label>
      <el-select v-model="local.categoryId" clearable placeholder="全部分类" style="width: 100%">
        <el-option
          v-for="cat in meta?.categories ?? []"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
    </div>

    <div class="field">
      <label>品牌</label>
      <el-select v-model="local.brandId" clearable placeholder="全部品牌" style="width: 100%">
        <el-option
          v-for="brand in meta?.brands ?? []"
          :key="brand.id"
          :label="brand.name"
          :value="brand.id"
        />
      </el-select>
    </div>

    <div class="field">
      <label>价格区间</label>
      <div class="price-range">
        <el-input-number
          v-model="local.minPrice"
          :min="0"
          :controls="false"
          placeholder="最低价"
        />
        <span>—</span>
        <el-input-number
          v-model="local.maxPrice"
          :min="0"
          :controls="false"
          placeholder="最高价"
        />
      </div>
    </div>

    <div class="actions">
      <el-button type="primary" @click="applyFilters">应用</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
  </aside>
</template>

<style scoped>
.filter-panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.filter-panel h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.field {
  margin-bottom: 16px;
}

.field label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-range span {
  color: #909399;
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
