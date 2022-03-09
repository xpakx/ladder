import { Injectable } from '@angular/core';
import { CollabTaskDetails } from '../entity/collab-task-details';
import { Task } from '../entity/task';
import { TaskDetails } from '../entity/task-details';
import { TaskTreeService } from './task-tree.service';

@Injectable({
  providedIn: 'root'
})
export class CollabTaskTreeService extends TaskTreeService {

  constructor() { super(); }

  loadTasks(tasks: CollabTaskDetails[]) {
    this.load(tasks.map((a) => this.transformCollabToTask(a)));
  }

  syncTasks(tasks: CollabTaskDetails[]) {
    this.sync(tasks.map((a) => this.transformCollabToTask(a)));
  }

  collapse(task: Task) {

  }

  private transformCollabToTask(task: CollabTaskDetails): TaskDetails {
    return {
      id: task.id,
      title: task.title,
      parent: task.parent,
      projectOrder: task.projectOrder,
      dailyViewOrder: 0,
      collapsed: task.collapsed,
      archived: task.archived,
      description: task.description, 
      project: task.project, 
      due: task.due ? new Date(task.due) : null, 
      completed: task.completed,
      labels: [],
      priority: task.priority,
      modifiedAt:  new Date(task.modifiedAt),
      assigned: task.assigned
    }
  }
}
