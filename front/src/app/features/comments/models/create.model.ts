import { FormControl } from '@angular/forms';

export interface CommentCreateRequest {
  content: string;
  userId: number | null;
  postId: number | null;
}

export type CommentCreateForm = {
  [K in keyof CommentCreateRequest]: FormControl<CommentCreateRequest[K]>;
};
