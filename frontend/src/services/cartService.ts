import api from './api';

export interface CartItem {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineSubtotal: number;
  available: boolean;
}

export interface Cart {
  items: CartItem[];
  subtotal: number;
  itemCount: number;
}

export interface AddCartItemPayload {
  productId: number;
  quantity?: number;
}

export async function getCart(): Promise<Cart> {
  const { data } = await api.get<Cart>('/cart');
  return data;
}

export async function addCartItem(payload: AddCartItemPayload): Promise<Cart> {
  const { data } = await api.post<Cart>('/cart/items', {
    productId: payload.productId,
    quantity: payload.quantity ?? 1,
  });
  return data;
}

export async function updateCartItem(productId: number, quantity: number): Promise<Cart> {
  const { data } = await api.put<Cart>(`/cart/items/${productId}`, { quantity });
  return data;
}

export async function removeCartItem(productId: number): Promise<Cart> {
  const { data } = await api.delete<Cart>(`/cart/items/${productId}`);
  return data;
}

export function getCartErrorMessage(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return '购物车操作失败，请稍后重试';
}
