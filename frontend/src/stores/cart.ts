import { defineStore } from 'pinia';
import {
  addCartItem,
  getCart,
  removeCartItem,
  updateCartItem,
  type Cart,
} from '@/services/cartService';

export const useCartStore = defineStore('cart', {
  state: () => ({
    cart: null as Cart | null,
    loading: false,
  }),

  getters: {
    itemCount: (state) => state.cart?.itemCount ?? 0,
    subtotal: (state) => state.cart?.subtotal ?? 0,
    hasUnavailableItems: (state) => state.cart?.items.some((item) => !item.available) ?? false,
    canCheckout: (state) =>
      (state.cart?.items.length ?? 0) > 0 &&
      !(state.cart?.items.some((item) => !item.available) ?? false),
  },

  actions: {
    async fetchCart() {
      this.loading = true;
      try {
        this.cart = await getCart();
      } finally {
        this.loading = false;
      }
    },

    async addItem(productId: number, quantity = 1) {
      this.cart = await addCartItem({ productId, quantity });
    },

    async updateQuantity(productId: number, quantity: number) {
      this.cart = await updateCartItem(productId, quantity);
    },

    async removeItem(productId: number) {
      this.cart = await removeCartItem(productId);
    },

    clearLocal() {
      this.cart = null;
    },
  },
});
