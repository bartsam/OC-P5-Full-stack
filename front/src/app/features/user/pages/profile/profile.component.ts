import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { TopicsListComponent } from '../../../topics/components/list/topics-list.component';
import { TopicItem } from '../../../topics/models';
import { TopicsService } from '../../../topics/services/topics.service';
import { User, UserUpdateForm } from '../../models';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-profile',
  imports: [MaterialComponents, ReactiveFormsModule, TopicsListComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly topicsService = inject(TopicsService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly notificationService = inject(NotificationService);
  private formBuilder = inject(FormBuilder);

  isPasswordVisible = false;
  readonly user = signal<User | null>(null);
  readonly topics = signal<TopicItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  public form: FormGroup<UserUpdateForm> = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(50)]],
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
    password: [
      '',
      [
        Validators.required,
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/),
      ],
    ],
  });

  ngOnInit(): void {
    this.userService
      .getUser()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: user => {
          this.user.set(user);
          this.form.patchValue({
            email: user.email,
            username: user.username,
          });
          this.loading.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.error.set(`Impossible de charger le profil : ${e.error?.message}`);
          this.loading.set(false);
        },
      });

    this.topicsService
      .getSubscribedTopics()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: topics => {
          this.topics.set(topics);
          this.loading.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.error.set(`Impossible de charger le thème : ${e.error?.message}`);
          this.loading.set(false);
        },
      });
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.userService
      .updateUser(this.form.getRawValue())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updatedUser => {
          this.user.set(updatedUser);
          this.notificationService.success('Profil mis à jour avec succès.');
          this.loading.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.notificationService.error(`Impossible de modifier le profil : ${e.error?.message}`);
          this.loading.set(false);
        },
      });
  }

  onUnsubscribe(topic: TopicItem): void {
    this.topicsService.unSubscribeTopic(topic.id).subscribe({
      next: () => {
        this.topics.update(topics => topics.filter(item => item.id !== topic.id) ?? []);
      },
      error: (e: HttpErrorResponse) =>
        this.notificationService.error(
          `Impossible de se désabonner de ce thème : ${e.error?.message}`,
        ),
    });
  }
}
