import { FormControl } from '@angular/forms';

export interface CommentCreateRequest {
  content: string;
}

export type CommentCreateForm = {
  [K in keyof CommentCreateRequest]: FormControl<CommentCreateRequest[K]>;
};
