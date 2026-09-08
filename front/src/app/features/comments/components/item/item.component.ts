import { Component, input } from '@angular/core';
import { MaterialComponents } from '@shared/ui/material';
import { CommentItem } from '../../models';

@Component({
  imports: [MaterialComponents],
  selector: 'app-comments-item',
  styleUrl: './item.component.scss',
  templateUrl: './item.component.html',
})
export class CommentsItemComponent {
  readonly comment = input.required<CommentItem>();
}
