import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { MaterialComponents } from '@shared/ui/material';
import { CommentCreateComponent } from '../../../comments/components/create/create.component';
import { CommentsListComponent } from '../../../comments/components/list/list.component';
import { PostDetail } from '../../models';
import { PostsService } from '../../services/posts.service';

@Component({
  imports: [DatePipe, MaterialComponents, CommentCreateComponent, CommentsListComponent],
  selector: 'app-post-detail',
  styleUrl: './detail.component.scss',
  templateUrl: './detail.component.html',
})
export class PostDetailComponent implements OnInit {
  private readonly postsService = inject(PostsService);
  private readonly destroyRef = inject(DestroyRef);
  private route = inject(ActivatedRoute);
  private readonly titleService = inject(Title);

  readonly postId = signal<string | null>(null);
  readonly post = signal<PostDetail | null>(null);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Identifiant de l’article invalide.');
      this.loading.set(false);
      return;
    }

    this.postId.set(id);

    this.postsService
      .getPost(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: post => {
          this.loading.set(false);
          this.titleService.setTitle(`${post.title} - MDD`);
          this.post.set(post);
        },
        error: (e: HttpErrorResponse) => {
          this.loading.set(false);
          this.error.set(`Impossible de charger l'article : ${e.error?.message}`);
        },
      });
  }
}
