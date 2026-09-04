import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
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

    const titleDe = fixture.debugElement.query(By.css('[data-testid="topic-title"]'));
    const descriptionDe = fixture.debugElement.query(By.css('[data-testid="topic-description"]'));

    expect(titleDe.nativeElement.textContent).toBe('Java');
    expect(descriptionDe.nativeElement.textContent).toBe('Langage de programmation orienté objet');
  });

  it('should show subscribe button when isSubscribable is true and isSubscribed is false', () => {
    fixture.componentRef.setInput('topic', mockTopic);
    fixture.componentRef.setInput('isSubscribable', true);
    fixture.detectChanges();

    const subscribeButton = fixture.debugElement.query(By.css('[data-testid="subscribe-button"]'));

    expect(subscribeButton).toBeTruthy();
    expect(subscribeButton.nativeElement.textContent).toContain("S'abonner");
    expect(subscribeButton.nativeElement.disabled).toBe(false);
  });

  it('should show disabled subscribe button when isSubscribed is true', () => {
    fixture.componentRef.setInput('topic', { ...mockTopic, isSubscribed: true });
    fixture.componentRef.setInput('isSubscribable', true);
    fixture.detectChanges();

    const subscribeButton = fixture.debugElement.query(By.css('[data-testid="subscribe-button"]'));

    expect(subscribeButton).toBeTruthy();
    expect(subscribeButton.nativeElement.textContent).toContain('Déjà abonné');
    expect(subscribeButton.nativeElement.disabled).toBe(true);
  });

  it('should show unsubscribe button when isSubscribable is false', () => {
    fixture.componentRef.setInput('topic', mockTopic);
    fixture.componentRef.setInput('isSubscribable', false);
    fixture.detectChanges();

    const unSubscribeButton = fixture.debugElement.query(
      By.css('[data-testid="unsubscribe-button"]'),
    );

    expect(unSubscribeButton).toBeTruthy();
    expect(unSubscribeButton.nativeElement.textContent).toContain('Se désabonner');
  });

  it('should emit the topic when the unsubscribe button is clicked', () => {
    fixture.componentRef.setInput('topic', mockTopic);
    fixture.componentRef.setInput('isSubscribable', false);
    fixture.detectChanges();

    let emittedTopic: TopicItem | undefined;
    component.handleSubscribe.subscribe(topic => (emittedTopic = topic));

    const unSubscribeButton = fixture.debugElement.query(
      By.css('[data-testid="unsubscribe-button"]'),
    );
    unSubscribeButton.nativeElement.click();

    expect(emittedTopic).toEqual(mockTopic);
  });
});
