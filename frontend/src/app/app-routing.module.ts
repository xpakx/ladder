import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ArchiveComponent } from './project/archive/archive.component';
import { CollabProjectComponent } from './project/collab-project/collab-project.component';
import { DailyViewComponent } from './schedule/daily-view/daily-view.component';
import { HabitListComponent } from './habit/habit-list/habit-list.component';
import { InboxComponent } from './project/inbox/inbox.component';
import { LabelComponent } from './label/label/label.component';
import { LoadProjectComponent } from './sync/load-project/load-project.component';
import { LoginComponent } from './user/login/login.component';
import { ProjectArchiveComponent } from './project/project-archive/project-archive.component';
import { ProjectComponent } from './project/project/project.component';
import { RegisterComponent } from './user/register/register.component';
import { SearchResultComponent } from './filter/search-result/search-result.component';
import { SettingsComponent } from './settings/settings/settings.component';
import { UpcomingComponent } from './schedule/upcoming/upcoming.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'load', component: LoadProjectComponent },
  { path: '', component: DailyViewComponent },
  { path: 'inbox', component: InboxComponent }, 
  { path: 'upcoming', component: UpcomingComponent }, 
  { path: 'project/:id', component: ProjectComponent },
  { path: 'project/:id/habits', component: HabitListComponent },
  { path: 'label/:id', component: LabelComponent },
  { path: 'search', component: SearchResultComponent },
  { path: 'archive', component: ArchiveComponent },
  { path: 'archive/project/:id', component: ProjectArchiveComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'collab/:id', component: CollabProjectComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
