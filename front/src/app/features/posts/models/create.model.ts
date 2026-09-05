import { FormControl } from '@angular/forms';

export interface PostCreateRequest {
  topicId: number;
  title: string;
  content: string;
}

export type PostCreateForm = {
  [K in keyof PostCreateRequest]: FormControl<PostCreateRequest[K]>;
};
