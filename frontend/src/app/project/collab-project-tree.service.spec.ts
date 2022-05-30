import { TestBed } from '@angular/core/testing';

import { CollabProjectTreeService } from './collab-project-tree.service';

describe('CollabProjectTreeService', () => {
  let service: CollabProjectTreeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CollabProjectTreeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
