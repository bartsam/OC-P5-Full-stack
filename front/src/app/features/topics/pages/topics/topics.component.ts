import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { TopicsListComponent } from '../../components/list/topics-list.component';
import { TopicItem } from '../../models';
import { TopicsService } from '../../services/topics.service';

@Component({
  imports: [MaterialComponents, TopicsListComponent],
  selector: 'app-topics',
  styleUrl: './topics.component.scss',
  templateUrl: './topics.component.html',
})
export class TopicsComponent implements OnInit {
  private readonly topicsService = inject(TopicsService);
  private readonly notificationService = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  readonly topics = signal<TopicItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.topicsService
      .getAllTopics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: topics => {
          this.topics.set(topics);
          this.loading.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.error.set(`Impossible de charger les topics : ${e.error?.message}`);
          this.loading.set(false);
        },
      });
  }

  onSubscribe(topic: TopicItem): void {
    this.topicsService.subscribeTopic(topic.id).subscribe({
      next: () => {
        this.topics.update(
          topics =>
            topics.map(item => (item.id === topic.id ? { ...item, isSubscribed: true } : item)) ??
            [],
        );
      },
      error: (e: HttpErrorResponse) =>
        this.notificationService.error(`Impossible de s'abonner à ce thème : ${e.error?.message}`),
    });
  }
}
