import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../../features/auth/services/auth.service';
import { MaterialComponents } from '../../ui/material';

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

  readonly isMenuOpen = signal(false);
  readonly showBackButton = signal(false);

  readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map(event => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  readonly isHomePage = computed(() => this.currentUrl() === '/');
  readonly isLoggedIn = this.authService.isLoggedIn;
  readonly hideHeader = computed(() => this.isHomePage() && !this.authService.isLoggedIn());

  constructor() {
    // Handle mobile enu
    effect(() => {
      const isOpen = this.isMenuOpen();

      if (isOpen) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.removeProperty('overflow');
      }
    });

    // Handle back button
    effect(() => {
      this.currentUrl();

      let route = this.router.routerState.root.snapshot;
      while (route.firstChild) {
        route = route.firstChild;
      }

      this.showBackButton.set(!!route.data['showBackButton']);
    });
  }

  toggleMenu(): void {
    this.isMenuOpen.update(open => !open);
  }

  closeMenu(): void {
    this.isMenuOpen.set(false);
  }

  goBack(): void {
    window.history.back();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
