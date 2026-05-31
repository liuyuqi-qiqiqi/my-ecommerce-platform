import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useCartStore } from '@/stores/cart';

vi.mock('@/services/cartService', () => ({
  getCart: vi.fn(),
  addCartItem: vi.fn(),
  updateCartItem: vi.fn(),
  removeCartItem: vi.fn(),
}));

import { getCart } from '@/services/cartService';

describe('cart store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('computes checkout eligibility from availability', async () => {
    vi.mocked(getCart).mockResolvedValue({
      items: [
        {
          productId: 1,
          productName: 'Phone',
          unitPrice: 999,
          quantity: 1,
          lineSubtotal: 999,
          available: true,
        },
      ],
      subtotal: 999,
      itemCount: 1,
    });

    const store = useCartStore();
    await store.fetchCart();

    expect(store.canCheckout).toBe(true);
    expect(store.itemCount).toBe(1);
  });

  it('blocks checkout when an item is unavailable', async () => {
    vi.mocked(getCart).mockResolvedValue({
      items: [
        {
          productId: 1,
          productName: 'Phone',
          unitPrice: 999,
          quantity: 1,
          lineSubtotal: 999,
          available: false,
        },
      ],
      subtotal: 999,
      itemCount: 1,
    });

    const store = useCartStore();
    await store.fetchCart();

    expect(store.canCheckout).toBe(false);
    expect(store.hasUnavailableItems).toBe(true);
  });
});
