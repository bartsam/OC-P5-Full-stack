import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { UserUpdateForm } from '../../models/update.model';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-profile',
  imports: [MaterialComponents, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly notificationService = inject(NotificationService);
  private formBuilder = inject(FormBuilder);

  isPasswordVisible = false;
  readonly user = signal<User | null>(null);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

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
          this.isLoading.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.errorMessage.set(`Impossible de charger le profil : ${e.error?.message}`);
          this.isLoading.set(false);
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
          this.isLoading.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.notificationService.error(`Impossible de modifier le profil : ${e.error?.message}`);
          this.isLoading.set(false);
        },
      });
  }
}
