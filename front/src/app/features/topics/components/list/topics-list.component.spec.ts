import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';

import { TopicItem } from '../../models';
import { TopicsListComponent } from './topics-list.component';

describe('TopicsListComponent', () => {
  let component: TopicsListComponent;
  let fixture: ComponentFixture<TopicsListComponent>;

  const mockTopics: TopicItem[] = [
    { id: 1, name: 'Java', description: 'Backend', isSubscribed: false },
    { id: 2, name: 'Angular', description: 'Frontend', isSubscribed: true },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopicsListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicsListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render one app-topics-item per topic', () => {
    fixture.componentRef.setInput('topics', mockTopics);
    fixture.detectChanges();

    const items = fixture.debugElement.queryAll(el => el.name === 'app-topics-item');
    expect(items.length).toBe(2);
  });

  it('should pass topic and isSubscribable inputs to each item', () => {
    fixture.componentRef.setInput('topics', mockTopics);
    fixture.componentRef.setInput('isSubscribable', false);
    fixture.detectChanges();

    const items = fixture.debugElement.queryAll(el => el.name === 'app-topics-item');
    expect(items.length).toBe(2);

    const firstItem = items[0].componentInstance;
    const secondItem = items[1].componentInstance;

    expect(firstItem.topic()).toEqual(mockTopics[0]);
    expect(firstItem.isSubscribable()).toBe(false);

    expect(secondItem.topic()).toEqual(mockTopics[1]);
    expect(secondItem.isSubscribable()).toBe(false);
  });

  it('should emit handleSubscribe with topic when item emits', () => {
    fixture.componentRef.setInput('topics', mockTopics);
    fixture.detectChanges();

    let emittedTopic: TopicItem | undefined;
    component.handleSubscribe.subscribe(topic => (emittedTopic = topic));

    const items = fixture.debugElement.queryAll(el => el.name === 'app-topics-item');
    const firstItemComponent = items[0].componentInstance;

    firstItemComponent.handleSubscribe.emit(mockTopics[0]);

    expect(emittedTopic).toEqual(mockTopics[0]);
  });

  it('should use default isSubscribable value of true when not provided', () => {
    fixture.componentRef.setInput('topics', mockTopics);
    fixture.detectChanges();

    const items = fixture.debugElement.queryAll(el => el.name === 'app-topics-item');
    const firstItem = items[0].componentInstance;

    expect(firstItem.isSubscribable()).toBe(true);
  });
});
