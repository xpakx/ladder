import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class RedirectionService {
  private lastUrl: string = "";

  constructor() { }

  setAddress(newUrl: string) {
    this.lastUrl = newUrl;
  }

  getAddress(): string {
    return this.lastUrl;
  }
}
