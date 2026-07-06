export type UserRole = "ROLE_ADMIN" | "ROLE_SELLER" | "ROLE_CLIENT";

export interface JwtToken {
  token: string;
}

export interface JwtClaims {
  sub?: string;
  email?: string;
  role?: UserRole | UserRole[];
  roles?: UserRole[];
  authorities?: string[];
  exp?: number;
}

export interface UserLoginDto {
  email: string;
  password: string;
}

export interface UserCreateDto {
  email: string;
  password: string;
  name: string;
  role: UserRole;
  phoneNumber: string;
  cpf: string;
}

export interface Product {
  id: string;
  sellerId: string;
  name: string;
  description?: string;
  price: number;
  stock: number;
  active: boolean;
  category: string;
  sku: string;
  attributes?: Record<string, unknown>;
  imageUrls?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ProductCreateDto {
  name: string;
  description?: string;
  price: number;
  stock: number;
  category: string;
  sku: string;
  attributes?: Record<string, unknown>;
  imageUrls?: string[];
}

export interface OrderItemDto {
  productId: string;
  quantity: number;
}

export interface OrderCreateDto {
  items: OrderItemDto[];
}

export interface OrderItemResponse {
  id: string;
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  createdAt?: string;
}

export interface SellerOrderResponse {
  id: string;
  sellerId: string;
  status: string;
  subTotal: number;
  items: OrderItemResponse[];
}

export interface OrderResponse {
  id: string;
  clientId: string;
  status: string;
  totalPrice: number;
  sellerOrders: SellerOrderResponse[];
  createdAt?: string;
  updatedAt?: string;
}

export interface PaymentResponse {
  id: string;
  orderId: string;
  clientId: string;
  totalPrice: number;
  status: string;
  paymentMethod: string;
  mpInitPoint?: string;
  mpSandboxInitPoint?: string;
}