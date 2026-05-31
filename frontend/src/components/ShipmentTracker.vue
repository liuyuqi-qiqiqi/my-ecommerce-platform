<script setup lang="ts">
import { computed } from 'vue';
import {
  formatOrderDate,
  getShipmentStatusLabel,
  type ShipmentInfo,
} from '@/services/orderService';

const props = defineProps<{
  shipment?: ShipmentInfo | null;
}>();

const STALE_HOURS = 48;

const statusLabel = computed(() =>
  props.shipment ? getShipmentStatusLabel(props.shipment.status) : '暂无物流信息',
);

const isStale = computed(() => {
  if (!props.shipment || props.shipment.status === 'DELIVERED') {
    return false;
  }
  const updated = new Date(props.shipment.statusUpdatedAt).getTime();
  const staleAfter = STALE_HOURS * 60 * 60 * 1000;
  return Date.now() - updated > staleAfter;
});

const statusType = computed(() => {
  if (!props.shipment) {
    return 'info';
  }
  switch (props.shipment.status) {
    case 'DELIVERED':
      return 'success';
    case 'IN_TRANSIT':
      return 'primary';
    case 'SHIPPED':
      return 'primary';
    default:
      return 'warning';
  }
});
</script>

<template>
  <section class="tracker">
    <h3>物流信息</h3>

    <template v-if="shipment">
      <div class="row">
        <span class="label">状态</span>
        <el-tag :type="statusType" size="small">{{ statusLabel }}</el-tag>
      </div>
      <div v-if="shipment.carrier" class="row">
        <span class="label">承运商</span>
        <span>{{ shipment.carrier }}</span>
      </div>
      <div v-if="shipment.trackingNumber" class="row">
        <span class="label">运单号</span>
        <span class="mono">{{ shipment.trackingNumber }}</span>
      </div>
      <div class="row">
        <span class="label">更新时间</span>
        <span>{{ formatOrderDate(shipment.statusUpdatedAt) }}</span>
      </div>
      <el-alert
        v-if="isStale"
        title="物流信息可能尚未更新，预计 1–2 个工作日内会有新进展"
        type="info"
        show-icon
        :closable="false"
        class="stale-msg"
      />
    </template>

    <el-empty v-else description="订单确认后将在此显示物流信息" :image-size="64" />
  </section>
</template>

<style scoped>
.tracker h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.label {
  color: #909399;
  flex-shrink: 0;
}

.mono {
  font-family: ui-monospace, monospace;
  word-break: break-all;
  text-align: right;
}

.stale-msg {
  margin-top: 12px;
}
</style>
