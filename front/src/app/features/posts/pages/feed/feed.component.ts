import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Sort } from '@shared/models/sort.type';
import { MaterialComponents } from '@shared/ui/material';
import { PostsItemComponent } from '../../components/item/item.component';
import { PostItem } from '../../models';
import { PostsService } from '../../services/posts.service';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
  imports: [MaterialComponents, RouterLink, PostsItemComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedComponent implements OnInit {
  private readonly postsService = inject(PostsService);
  private readonly destroyRef = inject(DestroyRef);

  readonly posts = signal<PostItem[]>([]);
  readonly currentSort = signal<Sort>('desc');
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadFeed();
  }

  private loadFeed(sort?: Sort): void {
    this.loading.set(true);
    this.error.set(null);

    const sortParam = sort ?? this.currentSort();

    this.postsService
      .getFeed(sortParam)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: posts => {
          this.loading.set(false);
          this.posts.set(posts);
        },
        error: (e: HttpErrorResponse) => {
          this.loading.set(false);
          this.error.set(`Impossible de charger les articles : ${e.error?.message}`);
        },
      });
  }

  toggleSort(): void {
    const next: Sort = this.currentSort() === 'asc' ? 'desc' : 'asc';
    this.currentSort.set(next);
    this.loadFeed(next);
  }
}
