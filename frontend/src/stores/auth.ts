import { defineStore } from 'pinia';
import { setAccessToken } from '@/services/api';
import {
  getProfile,
  login as loginApi,
  logout as logoutApi,
  refreshToken as refreshTokenApi,
  register as registerApi,
  updateProfile as updateProfileApi,
  type LoginPayload,
  type RegisterPayload,
  type UpdateProfilePayload,
  type UserProfile,
} from '@/services/authService';

const STORAGE_KEY = 'ecommerce_auth';

interface StoredAuth {
  accessToken: string;
  refreshToken: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null as string | null,
    refreshToken: null as string | null,
    profile: null as UserProfile | null,
    initialized: false,
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    displayName: (state) => state.profile?.displayName ?? '',
  },

  actions: {
    hydrateFromStorage() {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        this.initialized = true;
        return;
      }
      try {
        const stored = JSON.parse(raw) as StoredAuth;
        this.accessToken = stored.accessToken;
        this.refreshToken = stored.refreshToken;
        setAccessToken(stored.accessToken);
      } catch {
        localStorage.removeItem(STORAGE_KEY);
      }
      this.initialized = true;
    },

    persistTokens() {
      if (this.accessToken && this.refreshToken) {
        localStorage.setItem(
          STORAGE_KEY,
          JSON.stringify({ accessToken: this.accessToken, refreshToken: this.refreshToken }),
        );
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
      setAccessToken(this.accessToken);
    },

    applyAuthResponse(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
      this.persistTokens();
    },

    async register(payload: RegisterPayload) {
      const response = await registerApi(payload);
      this.applyAuthResponse(response.accessToken, response.refreshToken);
      await this.fetchProfile();
      const { useCartStore } = await import('@/stores/cart');
      await useCartStore().fetchCart();
    },

    async login(payload: LoginPayload) {
      const response = await loginApi(payload);
      this.applyAuthResponse(response.accessToken, response.refreshToken);
      await this.fetchProfile();
      const { useCartStore } = await import('@/stores/cart');
      await useCartStore().fetchCart();
    },

    async fetchProfile() {
      if (!this.accessToken) return;
      this.profile = await getProfile();
    },

    async updateProfile(payload: UpdateProfilePayload) {
      this.profile = await updateProfileApi(payload);
    },

    async refreshTokens(): Promise<string | null> {
      if (!this.refreshToken) {
        this.clearSession();
        return null;
      }
      try {
        const response = await refreshTokenApi(this.refreshToken);
        this.applyAuthResponse(response.accessToken, response.refreshToken);
        return response.accessToken;
      } catch {
        this.clearSession();
        return null;
      }
    },

    async logout() {
      if (this.refreshToken && this.accessToken) {
        try {
          await logoutApi(this.refreshToken);
        } catch {
          // ignore logout errors
        }
      }
      this.clearSession();
    },

    clearSession() {
      this.accessToken = null;
      this.refreshToken = null;
      this.profile = null;
      this.persistTokens();
    },
  },
});
