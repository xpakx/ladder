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
    for(let project of this.projects) {
      this.tasks = this.tasks.concat(project.tasks);
    }
  }
  
  getByDate(date: Date): TaskWithChildren[] {
    return this.tasks.filter((a) => {a.due === date});
  }
}
