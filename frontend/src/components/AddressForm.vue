<script setup lang="ts">
import { reactive, watch } from 'vue';
import type { AddressPayload } from '@/services/addressService';

const props = defineProps<{
  modelValue?: AddressPayload;
  editing?: boolean;
}>();

const emit = defineEmits<{
  submit: [payload: AddressPayload];
}>();

const form = reactive<AddressPayload>({
  recipientName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  street: '',
  postalCode: '',
  isDefault: false,
});

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      Object.assign(form, value);
    }
  },
  { immediate: true, deep: true },
);

function onSubmit() {
  emit('submit', { ...form });
}
</script>

<template>
  <el-form label-position="top" @submit.prevent="onSubmit">
    <el-form-item label="收件人" required>
      <el-input v-model="form.recipientName" maxlength="100" />
    </el-form-item>
    <el-form-item label="手机号" required>
      <el-input v-model="form.phone" maxlength="20" />
    </el-form-item>
    <div class="row">
      <el-form-item label="省" required>
        <el-input v-model="form.province" maxlength="50" />
      </el-form-item>
      <el-form-item label="市" required>
        <el-input v-model="form.city" maxlength="50" />
      </el-form-item>
    </div>
    <el-form-item label="区/县" required>
      <el-input v-model="form.district" maxlength="50" />
    </el-form-item>
    <el-form-item label="详细地址" required>
      <el-input v-model="form.street" maxlength="200" type="textarea" :rows="2" />
    </el-form-item>
    <el-form-item label="邮编" required>
      <el-input v-model="form.postalCode" maxlength="20" />
    </el-form-item>
    <el-form-item>
      <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
    </el-form-item>
    <el-button type="primary" native-type="submit">{{ editing ? '保存地址' : '添加地址' }}</el-button>
  </el-form>
</template>

<style scoped>
.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
</style>
