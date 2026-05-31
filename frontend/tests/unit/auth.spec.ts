import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '@/stores/auth';

vi.mock('@/services/api', () => ({
  default: { defaults: { headers: { common: {} } } },
  setAccessToken: vi.fn(),
}));

vi.mock('@/services/authService', () => ({
  login: vi.fn(),
  register: vi.fn(),
  getProfile: vi.fn(),
  logout: vi.fn(),
  refreshToken: vi.fn(),
}));

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('hydrates tokens from localStorage', () => {
    localStorage.setItem(
      'ecommerce_auth',
      JSON.stringify({ accessToken: 'access', refreshToken: 'refresh' }),
    );

    const store = useAuthStore();
    store.hydrateFromStorage();

    expect(store.isAuthenticated).toBe(true);
    expect(store.accessToken).toBe('access');
  });

  it('clears session on logout state reset', () => {
    const store = useAuthStore();
    store.accessToken = 'access';
    store.refreshToken = 'refresh';
    store.clearSession();

    expect(store.isAuthenticated).toBe(false);
    expect(localStorage.getItem('ecommerce_auth')).toBeNull();
  });
});
