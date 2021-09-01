import { Injectable } from '@angular/core';
import { FullProjectTree } from '../entity/full-project-tree';
import { TaskWithChildren } from '../entity/task-with-children';

@Injectable({
  providedIn: 'root'
})
export class TreeService {
  public projects: FullProjectTree[] = [];
  public tasks: TaskWithChildren[] = [];

  constructor() { }

  load(tree: FullProjectTree[]) {
    this.projects = tree;
    this.tasks = this.traverseProjects(tree);
  }

  traverseProjects(tree: FullProjectTree[]): TaskWithChildren[] {
    let tasks: TaskWithChildren[] = [];
    for(let project of this.projects) {
      tasks = tasks.concat(project.tasks).concat(this.traverseProjects(project.children));
    }
    return tasks;
  }
  
  getByDate(date: Date): TaskWithChildren[] {
    return this.tasks.filter((a) => {a.due === date});
  }
}
