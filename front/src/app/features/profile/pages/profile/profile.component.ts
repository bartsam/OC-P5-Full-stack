import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MaterialComponents } from '../../../../shared/material';
import { Profile } from '../../models/profile.model';
import { ProfileUpdateForm } from '../../models/update.model';
import { ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-profile',
  imports: [MaterialComponents, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit {
  private readonly profileService = inject(ProfileService);
  private formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  isPasswordVisible = false;
  readonly profile = signal<Profile | null>(null);
  readonly isLoading = signal(true);
  readonly onError = signal(false);
  readonly errorMessage = signal<string | null>(null);

  public form: FormGroup<ProfileUpdateForm> = this.formBuilder.nonNullable.group({
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
    this.profileService
      .getProfile()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: profile => {
          this.profile.set(profile);
          this.form.patchValue({
            email: profile.email,
            username: profile.username,
          });
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('Impossible de charger le profil.');
          this.isLoading.set(false);
        },
      });
  }

  submit(): void {
    console.log('profile PUT Request');
  }
}
