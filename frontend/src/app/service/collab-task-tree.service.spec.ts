import { TestBed } from '@angular/core/testing';

import { CollabTaskTreeService } from './collab-task-tree.service';

describe('CollabTaskTreeService', () => {
  let service: CollabTaskTreeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CollabTaskTreeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
