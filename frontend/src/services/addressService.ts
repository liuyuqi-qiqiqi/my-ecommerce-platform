import api from './api';

export interface Address {
  id: number;
  recipientName: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  street: string;
  postalCode: string;
  isDefault: boolean;
}

export interface AddressPayload {
  recipientName: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  street: string;
  postalCode: string;
  isDefault?: boolean;
}

export async function listAddresses(): Promise<Address[]> {
  const { data } = await api.get<Address[]>('/addresses');
  return data;
}

export async function createAddress(payload: AddressPayload): Promise<Address> {
  const { data } = await api.post<Address>('/addresses', payload);
  return data;
}

export async function updateAddress(addressId: number, payload: AddressPayload): Promise<Address> {
  const { data } = await api.put<Address>(`/addresses/${addressId}`, payload);
  return data;
}

export async function deleteAddress(addressId: number): Promise<void> {
  await api.delete(`/addresses/${addressId}`);
}

export function getAddressErrorMessage(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return '地址操作失败，请稍后重试';
}
