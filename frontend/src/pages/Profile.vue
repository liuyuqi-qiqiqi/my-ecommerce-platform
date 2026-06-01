<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import AppLayout from '@/components/AppLayout.vue';
import { getErrorMessage } from '@/services/authService';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

const form = reactive({
  displayName: '',
  email: '',
  phone: '',
});
const loading = ref(false);
const saving = ref(false);
const error = ref('');
const success = ref('');

onMounted(async () => {
  loading.value = true;
  try {
    await authStore.fetchProfile();
    if (authStore.profile) {
      form.displayName = authStore.profile.displayName;
      form.email = authStore.profile.email;
      form.phone = authStore.profile.phone ?? '';
    }
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
});

async function onSave() {
  error.value = '';
  success.value = '';
  saving.value = true;
  try {
    await authStore.updateProfile({
      displayName: form.displayName,
      email: form.email,
      phone: form.phone || undefined,
    });
    success.value = '资料已保存';
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <AppLayout>
  <div class="page">
    <div class="card">
      <h1>个人资料</h1>
      <div v-if="loading" class="state">加载中...</div>
      <el-form v-else label-position="top" @submit.prevent="onSave">
        <el-form-item label="显示名称">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" type="email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="msg" />
        <el-alert v-if="success" :title="success" type="success" show-icon :closable="false" class="msg" />
        <el-button type="primary" native-type="submit" :loading="saving">保存</el-button>
      </el-form>
    </div>
  </div>
</AppLayout>
</template>

<style scoped>
.page {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card h1 {
  margin: 0 0 24px;
  font-size: 22px;
}

.state {
  text-align: center;
  color: #909399;
  padding: 24px;
}

.msg {
  margin-bottom: 16px;
}
</style>
