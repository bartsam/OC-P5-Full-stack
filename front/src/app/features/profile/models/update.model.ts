import { FormControl } from '@angular/forms';

export interface ProfileUpdate {
  email: string;
  username: string;
  password: string;
}

export type ProfileUpdateForm = {
  [K in keyof ProfileUpdate]: FormControl<ProfileUpdate[K]>;
};
