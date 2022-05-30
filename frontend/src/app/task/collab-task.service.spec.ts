import { TestBed } from '@angular/core/testing';

import { CollabTaskService } from './collab-task.service';

describe('CollabTaskService', () => {
  let service: CollabTaskService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CollabTaskService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
