import { TestBed } from '@angular/core/testing';

import { LabelTreeService } from './label-tree.service';

describe('LabelTreeService', () => {
  let service: LabelTreeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LabelTreeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
