import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TokenResponse } from 'src/app/entity/token-response';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  form: FormGroup;
  public invalid: boolean = false;
  public message: string = '';

  constructor(private fb: FormBuilder, private service: AuthenticationService, 
    private router: Router, private tree: TreeService) { 
      this.form = this.fb.group({
        username: ['', Validators.required],
        password: ['', Validators.required],
        passwordRe: ['', Validators.required]
      });
    }

  ngOnInit(): void {
  }

  toLogin(): void {
    this.router.navigate(["/login"]);
  }

  signUp(): void {
    if(this.form.valid) {
      this.invalid = false;
      this.service.register({
        username: this.form.controls.username.value,
        password: this.form.controls.password.value,
        passwordRe: this.form.controls.passwordRe.value,
      }).subscribe(
        (response: TokenResponse) => {
          localStorage.setItem("token", response.token);
          localStorage.setItem("user_id", response.id);
          
          this.router.navigate([""]);
        },
        (error: HttpErrorResponse) => {
          this.message = error.error.message;
          this.invalid = true;
        }
      )
    } else {
      this.message = "Fields cannot be empty!";
      this.invalid = true;
    }
  }
}
