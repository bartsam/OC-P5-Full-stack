import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { MaterialComponents } from '@shared/ui/material';
import { filter, map } from 'rxjs';
import { AuthService } from '../../../features/auth/services/auth.service';
import { HeaderComponent } from '../header/header.component';

@Component({
  imports: [MaterialComponents, HeaderComponent],
  selector: 'app-layout',
  styleUrl: './layout.component.scss',
  templateUrl: './layout.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayoutComponent {
  private readonly router = inject(Router);
  protected readonly authService = inject(AuthService);

  readonly showBackButton = signal(false);
  readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map(event => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  readonly hideHeader = computed(() => this.currentUrl() === '/' && !this.authService.isLoggedIn());

  constructor() {
    effect(() => {
      this.currentUrl();

      let route = this.router.routerState.root.snapshot;
      while (route.firstChild) {
        route = route.firstChild;
      }

      this.showBackButton.set(!!route.data['showBackButton']);
    });
  }

  goBack(): void {
    window.history.back();
  }
}
