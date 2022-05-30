import { TestBed } from '@angular/core/testing';

import { HabitCompletionTreeService } from './habit-completion-tree.service';

describe('HabitCompletionTreeService', () => {
  let service: HabitCompletionTreeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HabitCompletionTreeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
