import { TestBed } from '@angular/core/testing';

import { ProjectTreeService } from './project-tree.service';

describe('ProjectTreeService', () => {
  let service: ProjectTreeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectTreeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
