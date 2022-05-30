import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  logged: boolean = false;

  constructor() { }

}
