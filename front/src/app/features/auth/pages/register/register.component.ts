import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MaterialComponents } from '../../../../shared/material';
import { NotificationService } from '../../../../shared/services/notification.service';
import { RegisterForm, RegisterRequest } from '../../models';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  imports: [MaterialComponents, ReactiveFormsModule],
  styleUrl: './register.component.scss',
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private formBuilder = inject(FormBuilder);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
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
        next: () => this.router.navigate(['/']),
        error: (e: HttpErrorResponse) =>
          this.notificationService.error(`Impossible de s'enregistrer : ${e.message}`),
      });
  }
}
