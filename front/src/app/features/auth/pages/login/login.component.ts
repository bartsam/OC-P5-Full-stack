import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MaterialComponents } from '../../../../shared/material';
import { AuthService } from '../../auth.service';
import { LoginForm, LoginRequest } from '../../models';

@Component({
  imports: [RouterLink, MaterialComponents, ReactiveFormsModule],
  selector: 'app-login',
  styleUrl: './login.component.scss',
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private authService = inject(AuthService);
  private formBuilder = inject(FormBuilder);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  public onError = false;
  public errorMessage: string | null = null;
  public isPasswordVisible = false;

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

  submit(): void {
    this.onError = false;
    this.errorMessage = null;

    if (this.form.invalid) {
      return;
    }

    const loginRequest: LoginRequest = this.form.getRawValue();

    this.authService
      .login(loginRequest)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.router.navigate(['/home']),
        error: (e: HttpErrorResponse) => {
          this.onError = true;
          this.errorMessage = e.message ?? 'An internal error has occurred';
        },
      });
  }
}
