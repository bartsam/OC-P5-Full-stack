export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export interface LoginRequest {
  identifier: string;
  password: string;
}
