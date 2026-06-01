<script setup lang="ts">
import { reactive, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import AppLayout from '@/components/AppLayout.vue';
import { getErrorMessage } from '@/services/authService';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const form = reactive({
  email: '',
  password: '',
});
const loading = ref(false);
const error = ref('');

async function onSubmit() {
  error.value = '';
  loading.value = true;
  try {
    await authStore.login({ email: form.email, password: form.password });
    const redirect = (route.query.redirect as string) || '/';
    await router.replace(redirect);
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <AppLayout>
  <div class="page">
    <div class="card">
      <h1>登录</h1>
      <el-form label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" type="email" autocomplete="email" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="error" />
        <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
          登录
        </el-button>
      </el-form>
      <p class="footer">
        还没有账号？
        <RouterLink to="/register">立即注册</RouterLink>
      </p>
    </div>
  </div>
</AppLayout>
</template>

<style scoped>
.page {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card h1 {
  margin: 0 0 24px;
  font-size: 22px;
  text-align: center;
}

.error {
  margin-bottom: 16px;
}

.footer {
  margin: 20px 0 0;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.footer a {
  color: #409eff;
  text-decoration: none;
}
</style>
