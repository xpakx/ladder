import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserWithData } from 'src/app/sync/dto/user-with-data';
import { LoginService } from '../../user/login.service';
import { NotificationService } from 'src/app/sync/notification.service';
import { ProjectService } from '../../project/project.service';
import { RedirectionService } from 'src/app/utils/redirection.service';
import { TreeService } from 'src/app/utils/tree.service';

@Component({
  selector: 'app-load-project',
  templateUrl: './load-project.component.html',
  styleUrls: ['./load-project.component.css']
})
export class LoadProjectComponent implements OnInit {

  constructor(private router: Router, private tree: TreeService,
    private service: ProjectService, private redirService: RedirectionService, 
    private notifications: NotificationService, private login: LoginService) { }

  ngOnInit(): void {
    let id = localStorage.getItem('user_id');
    this.notifications.subscribe();
    if(id) {
      this.service.getFullInfo().subscribe(
        (response: UserWithData) => {
          this.tree.load(response);
          localStorage.setItem("username", response.username);
          this.router.navigate([this.redirService.getAddress()]);
          this.login.logged = true;
        },
        (error: HttpErrorResponse) => {
          if(error.status === 401) {
            localStorage.removeItem("token");
            localStorage.removeItem("user_id");
            localStorage.removeItem("username");
            this.router.navigate(['login']);
          }
          this.login.logged = false;
        }
      )
    } else {
      this.login.logged = false;
      this.router.navigate(["/login"]);
    }
  }

}
