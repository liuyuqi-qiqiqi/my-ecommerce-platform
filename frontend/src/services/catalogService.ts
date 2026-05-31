import api from './api';

export interface ProductSummary {
  id: number;
  name: string;
  price: number;
  brandName?: string;
  primaryImageUrl: string;
  inStock: boolean;
}

export interface ProductDetail extends ProductSummary {
  description: string;
  categoryName: string;
  stockQuantity: number;
}

export interface ProductSearchResult {
  items: ProductSummary[];
  page: number;
  pageSize: number;
  total: number;
}

export interface FilterOption {
  id: number;
  name: string;
}

export interface CatalogFilterMeta {
  categories: FilterOption[];
  brands: FilterOption[];
}

export interface SearchParams {
  q?: string;
  categoryId?: number;
  brandId?: number;
  minPrice?: number;
  maxPrice?: number;
  page?: number;
  pageSize?: number;
}

export async function getFeaturedProducts(): Promise<ProductSummary[]> {
  const { data } = await api.get<ProductSummary[]>('/products/featured');
  return data;
}

export async function searchProducts(params: SearchParams): Promise<ProductSearchResult> {
  const { data } = await api.get<ProductSearchResult>('/products/search', { params });
  return data;
}

export async function getProductById(productId: number): Promise<ProductDetail> {
  const { data } = await api.get<ProductDetail>(`/products/${productId}`);
  return data;
}

export async function getCatalogFilters(): Promise<CatalogFilterMeta> {
  const { data } = await api.get<CatalogFilterMeta>('/products/filters');
  return data;
}
