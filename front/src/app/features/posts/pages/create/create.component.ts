import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NotificationService } from '@shared/services/notification.service';
import { MaterialComponents } from '@shared/ui/material';
import { TopicOption } from '../../../topics/models';
import { TopicsService } from '../../../topics/services/topics.service';
import { PostCreateForm } from '../../models';
import { PostsService } from '../../services/posts.service';

@Component({
  imports: [MaterialComponents, ReactiveFormsModule],
  selector: 'app-create-post',
  styleUrl: './create.component.scss',
  templateUrl: './create.component.html',
})
export class PostCreateComponent implements OnInit {
  private readonly postsService = inject(PostsService);
  private readonly topicsService = inject(TopicsService);
  private router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly notificationService = inject(NotificationService);
  private formBuilder = inject(FormBuilder);

  readonly topics = signal<TopicOption[] | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  public form: FormGroup<PostCreateForm> = this.formBuilder.nonNullable.group({
    topicId: [null as number | null, [Validators.required]],
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]],
    content: ['', [Validators.required]],
  });

  ngOnInit(): void {
    this.topicsService
      .getTopicOptions()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: topics => {
          this.loading.set(false);
          this.topics.set(topics);
        },
        error: (e: HttpErrorResponse) => {
          this.loading.set(false);
          this.error.set(`Impossible de charger les topics : ${e.error?.message}`);
        },
      });
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);

    this.postsService
      .createPost(this.form.getRawValue())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Article créé avec succès.');
          this.loading.set(false);
          this.router.navigate(['/posts/feed']);
        },
        error: (e: HttpErrorResponse) => {
          this.notificationService.error(`Impossible de créer l'article : ${e.error?.message}`);
          this.loading.set(false);
        },
      });
  }
}
