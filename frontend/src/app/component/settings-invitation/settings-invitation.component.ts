import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Collaboration } from 'src/app/entity/collaboration';
import { CollaborationDetails } from 'src/app/entity/collaboration-details';
import { User } from 'src/app/entity/user';
import { CollaborationService } from 'src/app/service/collaboration.service';

@Component({
  selector: 'app-settings-invitation',
  templateUrl: './settings-invitation.component.html',
  styleUrls: ['./settings-invitation.component.css']
})
export class SettingsInvitationComponent implements OnInit {
  invitations: CollaborationDetails[] = [];
  token?: string;

  constructor(private service: CollaborationService) { }

  ngOnInit(): void {
    this.service.getInvitations().subscribe(
      (response: CollaborationDetails[]) => {
        this.invitations = response;
      },
      (error: HttpErrorResponse) => {

      }
    );
    this.service.getToken().subscribe(
      (response: string) => {
        this.token = response;
      },
      (error: HttpErrorResponse) => {

      }
    );
  }

  accept(id: number) {
    this.service.changeAcceptation(id, {flag: true}).subscribe(
      (response: Collaboration) => {
        this.invitations = this.invitations.filter((a) => a.id != response.id);
      },
      (error: HttpErrorResponse) => {

      }
    )
  }

  newToken() {
    this.service.getNewToken().subscribe(
      (response: User) => {
        this.token = response.collaborationToken;
      },
      (error: HttpErrorResponse) => {

      }
    )
  }

}
