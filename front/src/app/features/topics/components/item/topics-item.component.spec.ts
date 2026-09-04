import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { MaterialComponents } from '../../../../shared/material';
import { TopicItem } from '../../models';
import { TopicsItemComponent } from './topics-item.component';

describe('TopicsItemComponent', () => {
  let component: TopicsItemComponent;
  let fixture: ComponentFixture<TopicsItemComponent>;

  const mockTopic: TopicItem = {
    id: 1,
    name: 'Java',
    description: 'Langage de programmation orienté objet',
    isSubscribed: false,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopicsItemComponent, MaterialComponents],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicsItemComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display topic title and description', () => {
    fixture.componentRef.setInput('topic', mockTopic);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-testid="topic-title"]').textContent).toBe(
      'Java',
    );
    expect(
      fixture.nativeElement.querySelector('[data-testid="topic-description"]').textContent,
    ).toBe('Langage de programmation orienté objet');
  });

  it('should show subscribe button when isSubscribable is true and isSubscribed is false', () => {
    fixture.componentRef.setInput('topic', mockTopic);
    fixture.componentRef.setInput('isSubscribable', true);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('[data-testid="subscribe-button"]');
    expect(button).toBeTruthy();
    expect(button.textContent).toContain("S'abonner");
    expect(button.disabled).toBe(false);
  });

  it('should show disabled subscribe button when isSubscribed is true', () => {
    fixture.componentRef.setInput('topic', { ...mockTopic, isSubscribed: true });
    fixture.componentRef.setInput('isSubscribable', true);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('[data-testid="subscribe-button"]');
    expect(button).toBeTruthy();
    expect(button.textContent).toContain('Déjà abonné');
    expect(button.disabled).toBe(true);
  });

  it('should show unsubscribe button when isSubscribable is false', () => {
    fixture.componentRef.setInput('topic', mockTopic);
    fixture.componentRef.setInput('isSubscribable', false);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('[data-testid="unsubscribe-button"]');
    expect(button).toBeTruthy();
    expect(button.textContent).toContain('Se désabonner');
  });

  it('should emit handleSubscribe with topic on button click', () => {
    fixture.componentRef.setInput('topic', mockTopic);
    fixture.componentRef.setInput('isSubscribable', false);
    fixture.detectChanges();

    let emittedTopic: TopicItem | undefined;
    component.handleSubscribe.subscribe(topic => (emittedTopic = topic));

    fixture.nativeElement.querySelector('[data-testid="unsubscribe-button"]').click();

    expect(emittedTopic).toEqual(mockTopic);
  });
});
