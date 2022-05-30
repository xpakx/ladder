import { TestBed } from '@angular/core/testing';

import { RedirectionService } from './redirection.service';

describe('RedirectionService', () => {
  let service: RedirectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RedirectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
