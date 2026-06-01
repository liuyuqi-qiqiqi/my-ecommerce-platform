import api from './api';

export interface OrderItem {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineSubtotal: number;
}

export interface OrderAddressSnapshot {
  recipientName: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  street: string;
  postalCode: string;
}

export interface ShipmentInfo {
  carrier?: string | null;
  trackingNumber?: string | null;
  status: string;
  statusUpdatedAt: string;
}

export interface OrderSummary {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  createdAt: string;
}

export interface OrderPage {
  items: OrderSummary[];
  page: number;
  pageSize: number;
  total: number;
}

export interface OrderDetail {
  id: number;
  orderNumber: string;
  status: string;
  subtotal: number;
  shippingFee: number;
  totalAmount: number;
  paymentMethod: string;
  createdAt: string;
  items: OrderItem[];
  address: OrderAddressSnapshot;
  shipment?: ShipmentInfo | null;
}

export interface CheckoutPayload {
  addressId: number;
}

export class StockConflictError extends Error {
  code = 'STOCK_CONFLICT';

  constructor(message: string) {
    super(message);
    this.name = 'StockConflictError';
  }
}

const ORDER_STATUS_LABELS: Record<string, string> = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  CANCELLED: '已取消',
  SHIPPED: '已发货',
  DELIVERED: '已送达',
};

const SHIPMENT_STATUS_LABELS: Record<string, string> = {
  PENDING: '待发货',
  SHIPPED: '已发货',
  IN_TRANSIT: '运输中',
  DELIVERED: '已送达',
};

export function getOrderStatusLabel(status: string): string {
  return ORDER_STATUS_LABELS[status] ?? status;
}

export function getShipmentStatusLabel(status: string): string {
  return SHIPMENT_STATUS_LABELS[status] ?? status;
}

export async function checkout(payload: CheckoutPayload): Promise<OrderDetail> {
  try {
    const { data } = await api.post<OrderDetail>('/orders/checkout', payload);
    return data;
  } catch (error) {
    if (typeof error === 'object' && error !== null && 'response' in error) {
      const response = (error as { response?: { data?: { code?: string; message?: string } } }).response;
      if (response?.data?.code === 'STOCK_CONFLICT') {
        throw new StockConflictError(response.data.message ?? '部分商品库存不足');
      }
    }
    throw error;
  }
}

export async function listOrders(page = 1, pageSize = 10): Promise<OrderPage> {
  const { data } = await api.get<OrderPage>('/orders', { params: { page, pageSize } });
  return data;
}

export async function getOrder(orderId: number): Promise<OrderDetail> {
  const { data } = await api.get<OrderDetail>(`/orders/${orderId}`);
  return data;
}

export async function cancelOrder(orderId: number): Promise<void> {
  await api.post(`/orders/${orderId}/cancel`);
}

export function getOrderErrorMessage(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return '订单操作失败，请稍后重试';
}

export function formatOrderDate(iso: string): string {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(iso));
}

export function formatPrice(price: number): string {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(price);
}

export function getPaymentMethodLabel(method: string): string {
  if (method === 'PAY_ON_DELIVERY') {
    return '货到付款';
  }
  return method;
}
