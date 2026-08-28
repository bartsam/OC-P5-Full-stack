import { FormControl } from '@angular/forms';

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
}

export type RegisterForm = {
  [K in keyof RegisterRequest]: FormControl<RegisterRequest[K]>;
};
