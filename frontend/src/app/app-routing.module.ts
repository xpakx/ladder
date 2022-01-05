import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ArchiveComponent } from './component/archive/archive.component';
import { DailyViewComponent } from './component/daily-view/daily-view.component';
import { HabitListComponent } from './component/habit-list/habit-list.component';
import { InboxComponent } from './component/inbox/inbox.component';
import { LabelComponent } from './component/label/label.component';
import { LoadProjectComponent } from './component/load-project/load-project.component';
import { LoginComponent } from './component/login/login.component';
import { ProjectComponent } from './component/project/project.component';
import { RegisterComponent } from './component/register/register.component';
import { SearchResultComponent } from './component/search-result/search-result.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'load', component: LoadProjectComponent },
  { path: '', component: DailyViewComponent },
  { path: 'inbox', component: InboxComponent }, 
  { path: 'upcoming', component: DailyViewComponent }, // todo
  { path: 'project/:id', component: ProjectComponent },
  { path: 'project/:id/habits', component: HabitListComponent },
  { path: 'label/:id', component: LabelComponent },
  { path: 'search', component: SearchResultComponent },
  { path: 'archive', component: ArchiveComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
