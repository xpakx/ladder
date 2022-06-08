import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TokenResponse } from '../dto/token-response';
import { AuthenticationService } from '../authentication.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  form: UntypedFormGroup;
  public invalid: boolean = false;
  public message: string = '';
  loading: boolean = false;

  constructor(private fb: UntypedFormBuilder, private service: AuthenticationService, 
    private router: Router) { 
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
