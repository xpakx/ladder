import { Injectable } from '@angular/core';
import { LabelDetails } from '../entity/label-details';
import { ProjectDetails } from '../entity/project-details';
import { TaskDetails } from '../entity/task-details';
import { UserWithData } from '../entity/user-with-data';

@Injectable({
  providedIn: 'root'
})
export class TreeService {
  public projects: ProjectDetails[] = [];
  public tasks: TaskDetails[] = [];
  public labels: LabelDetails[] = [];

  constructor() { }

  load(tree: UserWithData) {
    this.projects = tree.projects;
    this.tasks = tree.tasks;
    this.labels = tree.labels;
  }
  
  getByDate(date: Date): TaskDetails[] {
    return this.tasks.filter((a) => {
      a.due.getDate() === date.getDate() && a.due.getMonth() === date.getMonth() && a.due.getFullYear() === date.getFullYear() 
    });
  }
}
