import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { LoginForm, LoginRequest } from '../../models';
import { AuthService } from '../../services/auth.service';

@Component({
  imports: [MaterialComponents, ReactiveFormsModule],
  selector: 'app-login',
  styleUrl: './login.component.scss',
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private authService = inject(AuthService);
  private formBuilder = inject(FormBuilder);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private readonly notificationService = inject(NotificationService);

  readonly isPasswordVisible = signal(false);

  public form: FormGroup<LoginForm> = this.formBuilder.nonNullable.group({
    identifier: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
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

    const loginRequest: LoginRequest = this.form.getRawValue();

    this.authService
      .login(loginRequest)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.router.navigate(['/']),
        error: (e: HttpErrorResponse) =>
          this.notificationService.error(`Impossible de se connecter : ${e.message}`),
      });
  }
}
