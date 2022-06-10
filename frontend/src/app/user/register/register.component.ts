import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TokenResponse } from '../dto/token-response';
import { AuthenticationService } from '../authentication.service';

export interface RegisterForm {
  username: FormControl<string>;
  password: FormControl<string>;
  passwordRe: FormControl<string>;
}

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  form: FormGroup<RegisterForm>;
  public invalid: boolean = false;
  public message: string = '';
  loading: boolean = false;

  constructor(private fb: FormBuilder, private service: AuthenticationService, 
    private router: Router) { 
      this.form = this.fb.nonNullable.group({
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
      this.loading = true;
      this.invalid = false;
      this.service.register({
        username: this.form.controls.username.value,
        password: this.form.controls.password.value,
        passwordRe: this.form.controls.passwordRe.value,
      }).subscribe(
        (response: TokenResponse) => {
          localStorage.setItem("token", response.token);
          localStorage.setItem("user_id", response.id);
          this.loading = false;          
          this.router.navigate(["load"]);
        },
        (error: HttpErrorResponse) => {
          this.message = error.error.message;
          this.invalid = true;
          this.loading = false;
        }
      )
    } else {
      this.message = "Fields cannot be empty!";
      this.invalid = true;
    }
  }


  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent() {
    this.signUp();
  }
}
