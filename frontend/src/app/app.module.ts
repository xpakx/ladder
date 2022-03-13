import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { JwtModule } from '@auth0/angular-jwt';
import { LoginComponent } from './component/login/login.component';
import { RegisterComponent } from './component/register/register.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { DailyViewComponent } from './component/daily-view/daily-view.component';
import { LoadProjectComponent } from './component/load-project/load-project.component';
import { ProjectComponent } from './component/project/project.component';
import { TaskFormComponent } from './component/task-form/task-form.component';
import { DndModule } from 'ngx-drag-drop';
import { ProjectDialogComponent } from './component/project-dialog/project-dialog.component';
import { TaskDialogComponent } from './component/task-dialog/task-dialog.component';
import { DateDialogComponent } from './component/date-dialog/date-dialog.component';
import { LabelDialogComponent } from './component/label-dialog/label-dialog.component';
import { ProjectChoiceDialogComponent } from './component/project-choice-dialog/project-choice-dialog.component';
import { PriorityModalComponent } from './component/priority-modal/priority-modal.component';
import { ProjectListComponent } from './component/project-list/project-list.component';
import { LabelChoiceDialogComponent } from './component/label-choice-dialog/label-choice-dialog.component';
import { LabelListComponent } from './component/label-list/label-list.component';
import { DeleteDialogComponent } from './component/delete-dialog/delete-dialog.component';
import { SidebarComponent } from './component/sidebar/sidebar.component';
import { InboxComponent } from './component/inbox/inbox.component';
import { LabelComponent } from './component/label/label.component';
import { TaskViewComponent } from './component/task-view/task-view.component';
import { SubtaskListComponent } from './component/subtask-list/subtask-list.component';
import { CommentListComponent } from './component/comment-list/comment-list.component';
import { HabitListComponent } from './component/habit-list/habit-list.component';
import { TaskListComponent } from './component/task-list/task-list.component';
import { HabitFormComponent } from './component/habit-form/habit-form.component';
import { SearchResultComponent } from './component/search-result/search-result.component';
import { FilterListComponent } from './component/filter-list/filter-list.component';
import { FilterDialogComponent } from './component/filter-dialog/filter-dialog.component';
import { LabelSearchListComponent } from './component/label-search-list/label-search-list.component';
import { ProjectSearchListComponent } from './component/project-search-list/project-search-list.component';
import { ArchiveComponent } from './component/archive/archive.component';
import { ProjectArchiveComponent } from './component/project-archive/project-archive.component';
import { TaskDailyListComponent } from './component/task-daily-list/task-daily-list.component';
import { UpcomingComponent } from './component/upcoming/upcoming.component';
import { SettingsComponent } from './component/settings/settings.component';
import { SettingsExportComponent } from './component/settings-export/settings-export.component';
import { SettingsImportComponent } from './component/settings-import/settings-import.component';
import { EditCollabsComponent } from './component/edit-collabs/edit-collabs.component';
import { CollabProjectListComponent } from './component/collab-project-list/collab-project-list.component';
import { CollabProjectComponent } from './component/collab-project/collab-project.component';
import { CollabTaskListComponent } from './component/collab-task-list/collab-task-list.component';
import { ErrorInterceptor } from './utils/error.interceptor';
import { SettingsInvitationComponent } from './component/settings-invitation/settings-invitation.component';
import { AssignModalComponent } from './component/assign-modal/assign-modal.component';
import { CollabSubtaskListComponent } from './component/collab-subtask-list/collab-subtask-list.component';

export function tokenGetter() {
  return localStorage.getItem('token');
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    DailyViewComponent,
    LoadProjectComponent,
    ProjectComponent,
    TaskFormComponent,
    ProjectDialogComponent,
    TaskDialogComponent,
    DateDialogComponent,
    LabelDialogComponent,
    ProjectChoiceDialogComponent,
    PriorityModalComponent,
    ProjectListComponent,
    LabelChoiceDialogComponent,
    LabelListComponent,
    DeleteDialogComponent,
    SidebarComponent,
    InboxComponent,
    LabelComponent,
    TaskViewComponent,
    SubtaskListComponent,
    CommentListComponent,
    HabitListComponent,
    TaskListComponent,
    HabitFormComponent,
    SearchResultComponent,
    FilterListComponent,
    FilterDialogComponent,
    LabelSearchListComponent,
    ProjectSearchListComponent,
    ArchiveComponent,
    ProjectArchiveComponent,
    TaskDailyListComponent,
    UpcomingComponent,
    SettingsComponent,
    SettingsExportComponent,
    SettingsImportComponent,
    EditCollabsComponent,
    CollabProjectListComponent,
    CollabProjectComponent,
    CollabTaskListComponent,
    SettingsInvitationComponent,
    AssignModalComponent,
    CollabSubtaskListComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter,
        allowedDomains: ['localhost:8080', '192.168.50.118:8080'],
      }
    }),
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    DndModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
