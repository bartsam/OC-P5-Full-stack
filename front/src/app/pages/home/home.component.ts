import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MaterialComponents } from '@shared/ui/material';
import { AuthService } from '../../features/auth/services/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [RouterLink, MaterialComponents],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent {
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  readonly isLoggedIn = this.authService.isLoggedIn;

  constructor() {
    effect(() => {
      if (this.isLoggedIn()) {
        this.router.navigate(['/posts/feed']);
      }
    });
  }
}
