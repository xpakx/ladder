import { TestBed } from '@angular/core/testing';

import { KeyboardManagerService } from './keyboard-manager.service';

describe('KeyboardManagerService', () => {
  let service: KeyboardManagerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KeyboardManagerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
