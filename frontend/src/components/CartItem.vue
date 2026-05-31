<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import type { CartItem as CartItemType } from '@/services/cartService';

const props = defineProps<{
  item: CartItemType;
  updating?: boolean;
}>();

const emit = defineEmits<{
  updateQuantity: [quantity: number];
  remove: [];
}>();

const formatPrice = (price: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(price);

const maxQuantity = computed(() => (props.item.available ? 99 : props.item.quantity));

function onQuantityChange(value: number | undefined) {
  if (value != null && value >= 1) {
    emit('updateQuantity', value);
  }
}
</script>

<template>
  <div class="cart-item" :class="{ unavailable: !item.available }">
    <div class="info">
      <RouterLink :to="`/products/${item.productId}`" class="name">{{ item.productName }}</RouterLink>
      <p class="unit-price">单价：{{ formatPrice(item.unitPrice) }}</p>
      <el-tag v-if="!item.available" type="warning" size="small">库存不足或已下架</el-tag>
    </div>
    <div class="actions">
      <el-input-number
        :model-value="item.quantity"
        :min="1"
        :max="maxQuantity"
        :disabled="updating || !item.available"
        @change="onQuantityChange"
      />
      <p class="line-subtotal">{{ formatPrice(item.lineSubtotal) }}</p>
      <el-button type="danger" link :disabled="updating" @click="emit('remove')">删除</el-button>
    </div>
  </div>
</template>

<style scoped>
.cart-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.cart-item.unavailable {
  opacity: 0.85;
  background: #fdf6ec;
  margin: 0 -16px;
  padding: 16px;
  border-radius: 8px;
}

.info {
  flex: 1;
}

.name {
  font-weight: 500;
  color: #303133;
  text-decoration: none;
}

.name:hover {
  color: #409eff;
}

.unit-price {
  margin: 6px 0;
  font-size: 13px;
  color: #909399;
}

.actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.line-subtotal {
  margin: 0;
  font-weight: 600;
  color: #f56c6c;
}
</style>
