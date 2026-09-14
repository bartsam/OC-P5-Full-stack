import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  inject,
  input,
  OnInit,
  output,
  signal,
  ViewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormGroup,
  FormGroupDirective,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { NotificationService } from '@shared/services/notification.service';
import { MaterialComponents } from '@shared/ui/material';
import { CommentCreateForm, CommentItem } from '../../models';
import { CommentsService } from '../../services/comments.service';

@Component({
  imports: [MaterialComponents, ReactiveFormsModule],
  selector: 'app-create-comment',
  styleUrl: './create.component.scss',
  templateUrl: './create.component.html',
})
export class CommentCreateComponent implements OnInit {
  private readonly commentsService = inject(CommentsService);
  private readonly notificationService = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  readonly postId = input.required<number>();
  readonly commentCreated = output<CommentItem>();

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  form!: FormGroup<CommentCreateForm>;

  ngOnInit(): void {
    this.form = this.formBuilder.nonNullable.group({
      content: ['', [Validators.required]],
    });
  }

  @ViewChild(FormGroupDirective) formDirective!: FormGroupDirective;
  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);

    this.commentsService
      .createComment(this.form.getRawValue(), this.postId())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: comment => {
          this.notificationService.success('Commentaire créé avec succès.');
          this.loading.set(false);
          this.formDirective.resetForm();
          this.commentCreated.emit(comment);
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
