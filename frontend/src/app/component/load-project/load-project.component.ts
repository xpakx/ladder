import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserWithData } from 'src/app/entity/user-with-data';
import { NotificationService } from 'src/app/service/notification.service';
import { ProjectService } from 'src/app/service/project.service';
import { RedirectionService } from 'src/app/service/redirection.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-load-project',
  templateUrl: './load-project.component.html',
  styleUrls: ['./load-project.component.css']
})
export class LoadProjectComponent implements OnInit {

  constructor(private router: Router, private tree: TreeService,
    private service: ProjectService, private redirService: RedirectionService, 
    private notifications: NotificationService) { }

  ngOnInit(): void {
    let id = localStorage.getItem('user_id');
    this.notifications.subscribe();
    if(id) {
      this.service.getFullInfo().subscribe(
        (response: UserWithData) => {
          this.tree.load(response);
          this.router.navigate([this.redirService.getAddress()]);
          
        },
        (error: HttpErrorResponse) => {
          if(error.status === 401) {
            localStorage.removeItem("token");
            localStorage.removeItem("user_id");
            this.router.navigate(['login']);
          }
        }
      )
    } else {
      this.router.navigate(["/login"]);
    }
  }

}
