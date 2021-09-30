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
    ProjectChoiceDialogComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter,
        allowedDomains: ['localhost:8080', '192.168.1.204:8080'],
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
