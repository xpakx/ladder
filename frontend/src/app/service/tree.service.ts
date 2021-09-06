import { Injectable } from '@angular/core';
import { FullProjectTree } from '../entity/full-project-tree';
import { LabelDetails } from '../entity/label-details';
import { ProjectDetails } from '../entity/project-details';
import { TaskDetails } from '../entity/task-details';
import { TaskWithChildren } from '../entity/task-with-children';
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
    this.tasks.push({
      id: 1,
      title: "Test task",
      description: "Test task description",
      due: new Date(),
      project: {id: 5, name: "Project"},
      parent: {id: 5}
    });
    return this.tasks;
    //return this.tasks.filter((a) => {
    //  a.due.getDate() === date.getDate() && a.due.getMonth() === date.getMonth() && a.due.getFullYear() === date.getFullYear() 
    //});
  }
}
