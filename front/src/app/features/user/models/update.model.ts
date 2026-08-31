import { FormControl } from '@angular/forms';

export interface UserUpdateRequest {
  email: string;
  username: string;
  password: string;
}

export type UserUpdateForm = {
  [K in keyof UserUpdateRequest]: FormControl<UserUpdateRequest[K]>;
};
