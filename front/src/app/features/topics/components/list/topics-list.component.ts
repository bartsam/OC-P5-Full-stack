import { Component, input, output } from '@angular/core';
import { TopicItem } from '../../models';
import { TopicsItemComponent } from '../item/topics-item.component';

@Component({
  selector: 'app-topics-list',
  imports: [TopicsItemComponent],
  styleUrl: './topics-list.component.scss',
  templateUrl: './topics-list.component.html',
})
export class TopicsListComponent {
  readonly topics = input.required<TopicItem[]>();
  readonly isSubscribable = input<boolean>(true);
  readonly handleSubscribe = output<TopicItem>();
}
