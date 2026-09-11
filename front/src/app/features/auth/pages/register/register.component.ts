import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NotificationService } from '@shared/services/notification.service';
import { MaterialComponents } from '@shared/ui/material';
import { RegisterForm, RegisterRequest } from '../../models';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  imports: [MaterialComponents, ReactiveFormsModule],
  styleUrl: './register.component.scss',
  templateUrl: './register.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly notificationService = inject(NotificationService);

  readonly isPasswordVisible = signal(false);

  public form: FormGroup<RegisterForm> = this.formBuilder.nonNullable.group({
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

  togglePasswordVisibility(): void {
    this.isPasswordVisible.update(visible => !visible);
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    const registerRequest: RegisterRequest = this.form.getRawValue();

    this.authService
      .register(registerRequest)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.router.navigate(['/posts/feed']),
        error: (e: HttpErrorResponse) =>
          this.notificationService.error(`Impossible de s'enregistrer : ${e.message}`),
      });
  }
}
