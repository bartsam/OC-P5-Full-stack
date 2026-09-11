import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MaterialComponents } from '@shared/ui/material';
import { CommentItem } from '../../models';
import { CommentsService } from '../../services/comments.service';
import { CommentsItemComponent } from '../item/item.component';

@Component({
  imports: [MaterialComponents, CommentsItemComponent],
  selector: 'app-comments-list',
  styleUrl: './list.component.scss',
  templateUrl: './list.component.html',
})
export class CommentsListComponent implements OnInit {
  private readonly commentsService = inject(CommentsService);
  private readonly destroyRef = inject(DestroyRef);

  readonly postId = input.required<number>();
  readonly comments = signal<CommentItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.loading.set(true);
    this.error.set(null);

    this.commentsService
      .getComments(this.postId())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: posts => {
          this.loading.set(false);
          this.comments.set(posts);
        },
        error: (e: HttpErrorResponse) => {
          this.loading.set(false);
          this.error.set(`Impossible de charger les commentaires : ${e.error?.message}`);
        },
      });
  }

  addComment(comment: CommentItem): void {
    this.comments.update(current => [comment, ...current]);
  }
}
