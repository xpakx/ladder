import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TokenResponse } from '../dto/token-response';
import { AuthenticationService } from '../authentication.service';

export interface LoginForm {
  username: FormControl<string>;
  password: FormControl<string>;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  form: FormGroup<LoginForm>;
  public invalid: boolean = false;
  public message: string = '';
  loading: boolean = false;

  constructor(private fb: FormBuilder, private service: AuthenticationService, 
    private router: Router) { 
    this.form = this.fb.nonNullable.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
  }

  toRegister(): void {
    this.router.navigate(["/register"]);
  }

  logIn(): void {
    if(this.form.valid) {
      this.loading = true;
      this.invalid = false;
      this.service.authenticate({
        username: this.form.controls.username.value,
        password: this.form.controls.password.value
      }).subscribe(
        (response: TokenResponse) => {
          localStorage.setItem("token", response.token);
          localStorage.setItem("user_id", response.id);
          this.loading = false;
          this.router.navigate(["load"]);
        },
        (error: HttpErrorResponse) => {
          if(error.status === 401) {
            localStorage.removeItem("token");
            this.router.navigate(['login']);
          }
          this.loading = false;
          this.message = error.error.message;
          this.invalid = true;
        }
      )
    } else {
      this.message = "Fields cannot be empty!";
      this.invalid = true;
    }
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent() {
    this.logIn();
  }
}
