import { Component, input, output } from '@angular/core';
import { MaterialComponents } from '../../../../shared/material';
import { TopicItem } from '../../models';

@Component({
  imports: [MaterialComponents],
  selector: 'app-topics-item',
  styleUrl: './topics-item.component.scss',
  templateUrl: './topics-item.component.html',
})
export class TopicsItemComponent {
  readonly topic = input.required<TopicItem>();
  readonly isSubscribable = input<boolean>(true);
  readonly handleSubscribe = output<TopicItem>();

  onClick(): void {
    this.handleSubscribe.emit(this.topic());
  }
}
