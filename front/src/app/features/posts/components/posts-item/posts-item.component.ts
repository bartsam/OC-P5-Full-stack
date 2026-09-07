import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { MaterialComponents } from '@shared/ui/material';
import { PostItem } from '../../models';

@Component({
  imports: [MaterialComponents, DatePipe],
  selector: 'app-posts-item',
  styleUrl: './posts-item.component.scss',
  templateUrl: './posts-item.component.html',
})
export class PostsItemComponent {
  readonly post = input.required<PostItem>();
}
