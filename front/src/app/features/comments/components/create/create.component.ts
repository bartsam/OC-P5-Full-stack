import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NotificationService } from '@shared/services/notification.service';
import { MaterialComponents } from '@shared/ui/material';
import { CommentCreateForm } from '../../models';
import { CommentsService } from '../../services/comments.service';

@Component({
  imports: [MaterialComponents, ReactiveFormsModule],
  selector: 'app-create-comment',
  styleUrl: './create.component.scss',
  templateUrl: './create.component.html',
})
export class CommentCreateComponent implements OnInit {
  private readonly commentsService = inject(CommentsService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly notificationService = inject(NotificationService);
  private formBuilder = inject(FormBuilder);
  readonly postId = input.required<number>();

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  form!: FormGroup<CommentCreateForm>;

  ngOnInit(): void {
    this.form = this.formBuilder.nonNullable.group({
      content: ['', [Validators.required]],
      postId: [this.postId(), [Validators.required]],
    });
  }
  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);

    this.commentsService
      .createComment(this.form.getRawValue())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notificationService.success('Commentaire créé avec succès.');
          this.loading.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.notificationService.error(
            `Impossible de créer le commentaire : ${e.error?.message}`,
          );
          this.loading.set(false);
        },
      });
  }
}
