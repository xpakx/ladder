import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class KeyboardManagerService {
  public inInputMode: boolean = false;

  constructor() { }

  public blockLetters() {
    this.inInputMode = true;
  }

  public unblockLetters() {
    this.inInputMode = false;
  }
}
