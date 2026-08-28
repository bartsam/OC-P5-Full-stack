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

  readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map(event => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  readonly isHomePage = computed(() => this.currentUrl() === '/');
  readonly isLoggedIn = this.authService.isLoggedIn;
  readonly showBackButton = computed(() => ['/login', '/register'].includes(this.currentUrl()));
  readonly hideHeader = computed(() => this.isHomePage() && !this.authService.isLoggedIn());

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
