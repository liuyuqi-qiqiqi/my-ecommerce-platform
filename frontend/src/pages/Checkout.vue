<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import AddressForm from '@/components/AddressForm.vue';
import {
  createAddress,
  getAddressErrorMessage,
  listAddresses,
  type Address,
  type AddressPayload,
} from '@/services/addressService';
import { checkout, getOrderErrorMessage, StockConflictError } from '@/services/orderService';
import { useCartStore } from '@/stores/cart';

const router = useRouter();
const cartStore = useCartStore();

const addresses = ref<Address[]>([]);
const selectedAddressId = ref<number | null>(null);
const showAddressForm = ref(false);
const loading = ref(false);
const submitting = ref(false);
const error = ref('');

const formatPrice = (price: number) =>
  new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(price);

const shippingFee = computed(() => 10);
const totalAmount = computed(() => cartStore.subtotal + shippingFee.value);

onMounted(async () => {
  await Promise.all([loadAddresses(), cartStore.fetchCart()]);
  if (!cartStore.canCheckout) {
    error.value = '购物车为空或包含不可用商品，请返回购物车调整';
  }
});

async function loadAddresses() {
  loading.value = true;
  error.value = '';
  try {
    addresses.value = await listAddresses();
    const defaultAddress = addresses.value.find((item) => item.isDefault);
    selectedAddressId.value = defaultAddress?.id ?? addresses.value[0]?.id ?? null;
  } catch (err) {
    error.value = getAddressErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

async function onCreateAddress(payload: AddressPayload) {
  error.value = '';
  try {
    const created = await createAddress(payload);
    showAddressForm.value = false;
    await loadAddresses();
    selectedAddressId.value = created.id;
  } catch (err) {
    error.value = getAddressErrorMessage(err);
  }
}

async function onConfirmCheckout() {
  if (!selectedAddressId.value || !cartStore.canCheckout) {
    return;
  }
  submitting.value = true;
  error.value = '';
  try {
    const order = await checkout({ addressId: selectedAddressId.value });
    cartStore.clearLocal();
    await router.push({ name: 'order-confirmation', params: { id: order.id } });
  } catch (err) {
    if (err instanceof StockConflictError) {
      error.value = err.message;
      await cartStore.fetchCart();
      setTimeout(() => router.push('/cart'), 1500);
      return;
    }
    error.value = getOrderErrorMessage(err);
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <AppLayout>
  <div class="page">
    <h1>结算</h1>

    <div v-if="loading" class="state">加载中...</div>
    <template v-else>
      <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="msg" />

      <div class="layout">
        <section class="card">
          <div class="section-head">
            <h2>收货地址</h2>
            <el-button link type="primary" @click="showAddressForm = !showAddressForm">
              {{ showAddressForm ? '取消' : '新增地址' }}
            </el-button>
          </div>

          <AddressForm v-if="showAddressForm" @submit="onCreateAddress" />

          <el-radio-group v-else v-model="selectedAddressId" class="address-list">
            <el-radio v-for="address in addresses" :key="address.id" :value="address.id" class="address-item">
              <div>
                <strong>{{ address.recipientName }}</strong>
                <span class="phone">{{ address.phone }}</span>
                <el-tag v-if="address.isDefault" size="small" type="success">默认</el-tag>
              </div>
              <p>
                {{ address.province }}{{ address.city }}{{ address.district }}{{ address.street }}
                （{{ address.postalCode }}）
              </p>
            </el-radio>
          </el-radio-group>

          <el-empty v-if="!showAddressForm && !addresses.length" description="请先添加收货地址" />
        </section>

        <aside class="card summary">
          <h2>订单摘要</h2>
          <div v-if="cartStore.cart?.items.length">
            <div v-for="item in cartStore.cart.items" :key="item.productId" class="line">
              <span>{{ item.productName }} × {{ item.quantity }}</span>
              <span>{{ formatPrice(item.lineSubtotal) }}</span>
            </div>
          </div>
          <p class="row">
            <span>商品小计</span>
            <span>{{ formatPrice(cartStore.subtotal) }}</span>
          </p>
          <p class="row">
            <span>运费</span>
            <span>{{ formatPrice(shippingFee) }}</span>
          </p>
          <p class="row total">
            <span>应付总额</span>
            <span>{{ formatPrice(totalAmount) }}</span>
          </p>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="submitting"
            :disabled="!selectedAddressId || !cartStore.canCheckout"
            @click="onConfirmCheckout"
          >
            确认下单
          </el-button>
        </aside>
      </div>
    </template>
  </div>
  </AppLayout>
</template>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.page h1 {
  margin: 0 0 20px;
  font-size: 22px;
}

.layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-head h2,
.summary h2 {
  margin: 0;
  font-size: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.address-item {
  width: 100%;
  height: auto;
  margin-right: 0;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.address-item .phone {
  margin-left: 8px;
  color: #909399;
}

.address-item p {
  margin: 6px 0 0;
  color: #606266;
}

.line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #606266;
}

.row {
  display: flex;
  justify-content: space-between;
  margin: 0 0 12px;
  color: #606266;
}

.row.total {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.state {
  text-align: center;
  padding: 48px;
  color: #909399;
}

.msg {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
