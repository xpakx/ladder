import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { JwtModule } from '@auth0/angular-jwt';
import { LoginComponent } from './component/login/login.component';
import { RegisterComponent } from './component/register/register.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
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
    FilterDialogComponent
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
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
