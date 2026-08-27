import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MaterialComponents } from '../../../../shared/material';
import { AuthService } from '../../auth.service';
import { RegisterForm, RegisterRequest } from '../../models';

@Component({
  selector: 'app-register',
  imports: [RouterLink, MaterialComponents, ReactiveFormsModule],
  styleUrl: './register.component.scss',
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private formBuilder = inject(FormBuilder);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  public onError = false;
  public errorMessage: string | null = null;
  public isPasswordVisible = false;

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

  submit(): void {
    this.onError = false;
    this.errorMessage = null;

    if (this.form.invalid) {
      return;
    }

    const registerRequest: RegisterRequest = this.form.getRawValue();

    this.authService
      .register(registerRequest)
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
