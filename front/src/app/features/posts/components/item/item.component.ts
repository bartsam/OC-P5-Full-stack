import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MaterialComponents } from '@shared/ui/material';
import { PostItem } from '../../models';

@Component({
  imports: [MaterialComponents, DatePipe, RouterLink],
  selector: 'app-posts-item',
  styleUrl: './item.component.scss',
  templateUrl: './item.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PostsItemComponent {
  readonly post = input.required<PostItem>();
}
