import { TestBed } from '@angular/core/testing';

import { HabitTreeService } from './habit-tree.service';

describe('HabitTreeService', () => {
  let service: HabitTreeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HabitTreeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
