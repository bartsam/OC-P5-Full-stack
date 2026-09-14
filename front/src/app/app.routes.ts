import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/pages/login/login.component';
import { RegisterComponent } from './features/auth/pages/register/register.component';
import { HomeComponent } from './pages/home/home.component';

import { authGuard } from './core/auth/auth.guard';
import { PostCreateComponent } from './features/posts/pages/create/create.component';
import { PostDetailComponent } from './features/posts/pages/detail/detail.component';
import { FeedComponent } from './features/posts/pages/feed/feed.component';
import { TopicsComponent } from './features/topics/pages/topics/topics.component';
import { ProfileComponent } from './features/user/pages/profile/profile.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    title: 'Accueil - MDD',
  },

  {
    path: 'register',
    component: RegisterComponent,
    title: 'Inscription - MDD',
    data: { showBackButton: true },
  },
  {
    path: 'login',
    component: LoginComponent,
    title: 'Connexion - MDD',
    data: { showBackButton: true },
  },
  {
    path: 'profile',
    component: ProfileComponent,
    title: 'Mon profil - MDD',
    canActivate: [authGuard],
  },
  {
    path: 'topics',
    component: TopicsComponent,
    title: 'Thèmes - MDD',
    canActivate: [authGuard],
  },
  {
    path: 'posts',
    children: [
      {
        path: 'feed',
        component: FeedComponent,
        title: "Fil d'actualité - MDD",
        canActivate: [authGuard],
      },
      {
        path: 'create',
        component: PostCreateComponent,
        title: 'Créer un article - MDD',
        data: { showBackButton: true },
        canActivate: [authGuard],
      },
      {
        path: ':id',
        component: PostDetailComponent,
        title: 'Article - MDD',
        data: { showBackButton: true },
        canActivate: [authGuard],
      },
    ],
  },
  {
    path: '**',
    component: NotFoundComponent,
    title: 'Page introuvable - MDD',
  },
];
