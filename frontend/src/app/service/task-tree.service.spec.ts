import { TestBed } from '@angular/core/testing';

import { TaskTreeService } from './task-tree.service';

describe('TaskTreeService', () => {
  let service: TaskTreeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TaskTreeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
