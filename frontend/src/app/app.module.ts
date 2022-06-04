import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './user/login/login.component';
import { RegisterComponent } from './user/register/register.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { DailyViewComponent } from './schedule/daily-view/daily-view.component';
import { LoadProjectComponent } from './sync/load-project/load-project.component';
import { ProjectComponent } from './project/project/project.component';
import { TaskFormComponent } from './task/task-form/task-form.component';
import { DndModule } from 'ngx-drag-drop';
import { ProjectDialogComponent } from './project/project-dialog/project-dialog.component';
import { TaskDialogComponent } from './task/task-dialog/task-dialog.component';
import { DateDialogComponent } from './schedule/date-dialog/date-dialog.component';
import { LabelDialogComponent } from './label/label-dialog/label-dialog.component';
import { ProjectChoiceDialogComponent } from './project/project-choice-dialog/project-choice-dialog.component';
import { PriorityModalComponent } from './schedule/priority-modal/priority-modal.component';
import { ProjectListComponent } from './project/project-list/project-list.component';
import { LabelChoiceDialogComponent } from './label/label-choice-dialog/label-choice-dialog.component';
import { LabelListComponent } from './label/label-list/label-list.component';
import { DeleteDialogComponent } from './common/delete-dialog/delete-dialog.component';
import { SidebarComponent } from './views/sidebar/sidebar.component';
import { InboxComponent } from './project/inbox/inbox.component';
import { LabelComponent } from './label/label/label.component';
import { TaskViewComponent } from './task/task-view/task-view.component';
import { SubtaskListComponent } from './task/subtask-list/subtask-list.component';
import { CommentListComponent } from './comment/comment-list/comment-list.component';
import { HabitListComponent } from './habit/habit-list/habit-list.component';
import { TaskListComponent } from './task/task-list/task-list.component';
import { HabitFormComponent } from './habit/habit-form/habit-form.component';
import { SearchResultComponent } from './filter/search-result/search-result.component';
import { FilterListComponent } from './filter/filter-list/filter-list.component';
import { FilterDialogComponent } from './filter/filter-dialog/filter-dialog.component';
import { LabelSearchListComponent } from './label/label-search-list/label-search-list.component';
import { ProjectSearchListComponent } from './project/project-search-list/project-search-list.component';
import { ArchiveComponent } from './project/archive/archive.component';
import { ProjectArchiveComponent } from './project/project-archive/project-archive.component';
import { TaskDailyListComponent } from './task/task-daily-list/task-daily-list.component';
import { UpcomingComponent } from './schedule/upcoming/upcoming.component';
import { SettingsComponent } from './settings/settings/settings.component';
import { SettingsExportComponent } from './settings/settings-export/settings-export.component';
import { SettingsImportComponent } from './settings/settings-import/settings-import.component';
import { EditCollabsComponent } from './project/edit-collabs/edit-collabs.component';
import { CollabProjectListComponent } from './project/collab-project-list/collab-project-list.component';
import { CollabProjectComponent } from './project/collab-project/collab-project.component';
import { CollabTaskListComponent } from './task/collab-task-list/collab-task-list.component';
import { ErrorInterceptor } from './error/error.interceptor';
import { SettingsInvitationComponent } from './settings/settings-invitation/settings-invitation.component';
import { AssignModalComponent } from './collaboration/assign-modal/assign-modal.component';
import { CollabSubtaskListComponent } from './task/collab-subtask-list/collab-subtask-list.component';
import { AutofocusDirective } from './common/directives/autofocus.directive';
import { FocusableInputDirective } from './common/directives/focusable-input.directive';
import { MarkdownModule } from 'ngx-markdown';
import { ContextMenuComponent } from './context-menu/context-menu/context-menu.component';
import { DateLabelComponent } from './task/date-label/date-label.component';

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
    CollabSubtaskListComponent,
    AutofocusDirective,
    FocusableInputDirective,
    ContextMenuComponent,
    DateLabelComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    DndModule,
    MarkdownModule.forRoot(),
    BrowserAnimationsModule
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
