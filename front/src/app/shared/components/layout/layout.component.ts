import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../../features/auth/services/auth.service';
import { MaterialComponents } from '../../material';

@Component({
  imports: [MaterialComponents, RouterLink, RouterLinkActive],
  selector: 'app-layout',
  styleUrl: './layout.component.scss',
  templateUrl: './layout.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayoutComponent {
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  isHomePage = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(e => e.urlAfterRedirects === '/'),
    ),
    { initialValue: this.router.url === '/' },
  );

  readonly isLoggedIn = this.authService.isLoggedIn;

  readonly hideHeader = computed(() => this.isHomePage() && !this.authService.isLoggedIn());

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
