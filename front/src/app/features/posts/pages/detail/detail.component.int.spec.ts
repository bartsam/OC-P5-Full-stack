import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, DebugElement, Input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By, Title } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../../../environments/environment';
import { CommentCreateComponent } from '../../../comments/components/create/create.component';
import { CommentsListComponent } from '../../../comments/components/list/list.component';
import { PostDetail } from '../../models';
import { PostDetailComponent } from './detail.component';

@Component({ selector: 'app-comments-list', standalone: true, template: '' })
class CommentsListStubComponent {
  @Input() postId!: number;
}

@Component({ selector: 'app-create-comment', standalone: true, template: '' })
class CommentCreateStubComponent {
  @Input() postId!: number;
}

describe('PostDetailComponent integration', () => {
  let component: PostDetailComponent;
  let debugElement: DebugElement;
  let fixture: ComponentFixture<PostDetailComponent>;
  let httpMock: HttpTestingController;
  let mockTitleService: { setTitle: ReturnType<typeof vi.fn> };

  const apiUrl = `${environment.apiUrl}/posts`;

  const mockPost: PostDetail = {
    id: 123,
    title: 'Bien démarrer avec React Native et Expo',
    content: 'React Native permet de développer des applications mobiles…',
    author: 'jean-biche',
    topic: 'Mobile',
    createdAt: '2026-09-07T15:39:00',
  };

  function setupTestBed(activatedRouteIdMock: string) {
    mockTitleService = { setTitle: vi.fn() };

    TestBed.configureTestingModule({
      imports: [PostDetailComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Title, useValue: mockTitleService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id: activatedRouteIdMock }) },
          },
        },
      ],
    });

    TestBed.overrideComponent(PostDetailComponent, {
      remove: { imports: [CommentsListComponent, CommentCreateComponent] },
      add: { imports: [CommentsListStubComponent, CommentCreateStubComponent] },
    });

    fixture = TestBed.createComponent(PostDetailComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  }

  afterEach(() => {
    httpMock.verify();
    vi.clearAllMocks();
    TestBed.resetTestingModule();
  });

  it('should display the spinner, then render the post after GET succeeds', () => {
    setupTestBed('123');

    fixture.detectChanges();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeTruthy();

    const req = httpMock.expectOne(`${apiUrl}/123`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPost);
    fixture.detectChanges();

    expect(component.post()).toEqual(mockPost);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBeNull();

    expect(debugElement.query(By.css('[data-testid="loading-screen"]'))).toBeNull();

    const title = debugElement.query(By.css('[data-testid="post-title"]'));
    const author = debugElement.query(By.css('[data-testid="post-author"]'));
    const topic = debugElement.query(By.css('[data-testid="post-topic"]'));
    const content = debugElement.query(By.css('[data-testid="post-content"]'));

    expect(title.nativeElement.textContent).toContain(mockPost.title);
    expect(author.nativeElement.textContent).toContain(mockPost.author);
    expect(topic.nativeElement.textContent).toContain(mockPost.topic);
    expect(content.nativeElement.textContent).toContain(mockPost.content);

    expect(mockTitleService.setTitle).toHaveBeenCalledWith(`${mockPost.title} - MDD`);
  });

  it('should display the error screen when GET fails', () => {
    setupTestBed('123');

    fixture.detectChanges();

    const req = httpMock.expectOne(`${apiUrl}/123`);
    req.flush({ message: 'Not found' }, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.error()).toContain("Impossible de charger l'article");

    const errorScreen = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorScreen).toBeTruthy();
    expect(errorScreen.nativeElement.textContent).toContain("Impossible de charger l'article");

    expect(debugElement.query(By.css('[data-testid="post-title"]'))).toBeNull();
    expect(debugElement.query(By.css('[data-testid="post-content"]'))).toBeNull();
  });

  it('should display an error and skip the HTTP call when route id is missing', () => {
    setupTestBed('');

    fixture.detectChanges();

    httpMock.expectNone(`${apiUrl}/`);

    expect(component.loading()).toBe(false);
    expect(component.error()).toEqual('Identifiant de l’article invalide.');

    const errorScreen = debugElement.query(By.css('[data-testid="error-screen"]'));
    expect(errorScreen).toBeTruthy();
    expect(errorScreen.nativeElement.textContent).toContain('Identifiant de l’article invalide');

    expect(debugElement.query(By.css('[data-testid="post-title"]'))).toBeNull();
  });
});
