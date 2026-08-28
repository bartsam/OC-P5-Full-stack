import { FormControl } from '@angular/forms';

export interface LoginRequest {
  identifier: string;
  password: string;
}

export type LoginForm = {
  [K in keyof LoginRequest]: FormControl<LoginRequest[K]>;
};
