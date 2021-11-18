import { TestBed } from '@angular/core/testing';

import { FilterTreeService } from './filter-tree.service';

describe('FilterTreeService', () => {
  let service: FilterTreeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FilterTreeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
