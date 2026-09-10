import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MaterialComponents } from '@shared/ui/material';

@Component({
  imports: [RouterLink, MaterialComponents],
  selector: 'app-not-found',
  styleUrl: './not-found.component.scss',
  templateUrl: './not-found.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotFoundComponent {}
