import { Component, DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, RouterLink } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';

import { NotFoundComponent } from './not-found.component';

@Component({ template: '' })
class DummyComponent {}

describe('NotFoundComponent', () => {
  let component: NotFoundComponent;
  let fixture: ComponentFixture<NotFoundComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotFoundComponent],
      providers: [provideRouter([{ path: '', component: DummyComponent }])],
    }).compileComponents();

    fixture = TestBed.createComponent(NotFoundComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the 404 title', () => {
    const title = debugElement.query(By.css('h1'));
    expect(title.nativeElement.textContent).toContain('404');
  });

  it('should have a link back to home pointing to the root route', () => {
    const link = debugElement.query(By.directive(RouterLink));

    expect(link).toBeTruthy();
    expect(link.nativeElement.getAttribute('href')).toBe('/');
  });
});
