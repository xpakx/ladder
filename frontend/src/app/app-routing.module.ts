import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ArchiveComponent } from './component/archive/archive.component';
import { CollabProjectComponent } from './component/collab-project/collab-project.component';
import { DailyViewComponent } from './component/daily-view/daily-view.component';
import { HabitListComponent } from './component/habit-list/habit-list.component';
import { InboxComponent } from './component/inbox/inbox.component';
import { LabelComponent } from './component/label/label.component';
import { LoadProjectComponent } from './component/load-project/load-project.component';
import { LoginComponent } from './component/login/login.component';
import { ProjectArchiveComponent } from './component/project-archive/project-archive.component';
import { ProjectComponent } from './component/project/project.component';
import { RegisterComponent } from './component/register/register.component';
import { SearchResultComponent } from './component/search-result/search-result.component';
import { SettingsComponent } from './component/settings/settings.component';
import { UpcomingComponent } from './component/upcoming/upcoming.component';

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
