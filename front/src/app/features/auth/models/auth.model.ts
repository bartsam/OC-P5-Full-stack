import { FormControl } from '@angular/forms';

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
}

export type RegisterForm = {
  [K in keyof RegisterRequest]: FormControl<RegisterRequest[K]>;
};

export interface AuthResponse {
  token: string;
}

export interface LoginRequest {
  identifier: string;
  password: string;
}

export type LoginForm = {
  [K in keyof LoginRequest]: FormControl<LoginRequest[K]>;
};
