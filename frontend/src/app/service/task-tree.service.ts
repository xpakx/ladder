import { Injectable } from '@angular/core';
import { ParentWithId } from '../entity/parent-with-id';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { Task } from '../entity/task';
import { TaskDetails } from '../entity/task-details';
import { TaskTreeElem } from '../entity/task-tree-elem';
import { IndentableService } from './indentable-service';

@Injectable({
  providedIn: 'root'
})
export class TaskTreeService extends IndentableService<ParentWithId> {
  public list: TaskTreeElem[] = [];

  constructor() { super() }

  load(tasks: TaskDetails[]) {
    this.list = this.transformAll(tasks);
    this.sort();
  }

  transformAll(tasks: TaskDetails[]):  TaskTreeElem[] {
    return tasks.map((a) => this.transform(a, tasks));
  }

  transform(task: TaskDetails, tasks: TaskDetails[]): TaskTreeElem {
    let indent: number = this.getTaskIndent(task.id, tasks);
    return {
      id: task.id,
      title: task.title,
      parent: task.parent,
      order: task.projectOrder,
      realOrder: task.projectOrder,
      hasChildren: this.hasChildrenById(task.id, tasks),
      indent: indent,
      parentList: [],
      collapsed: false,
      description: task.description, 
      project: task.project, 
      due: task.due ? new Date(task.due) : null, 
      completed: task.completed
    }
  }

  getTaskIndent(taskId: number, tasks: TaskDetails[]): number {
    let parentId: number | undefined = tasks.find((a) => a.id == taskId)?.parent?.id;
    let counter = 0;
    while(parentId != null) {
      counter +=1;
      parentId = tasks.find((a) => a.id == parentId)?.parent?.id;
    }
    return counter;
  }

  hasChildrenById(taskId: number, tasks: TaskDetails[]): boolean {
    return tasks.find((a) => a.parent?.id == taskId) != null;
  }

  getByDate(date: Date): TaskTreeElem[] {
    return this.list.filter((a) => 
      a.due && a.due.getDate() === date.getDate() && a.due.getMonth() === date.getMonth() && a.due.getFullYear() === date.getFullYear() 
    );
  }

  getNumOfUncompletedTasksByProject(projectId: number): number {
    return this.list.filter((a) => 
      a.project && a.project.id == projectId && !a.completed
    ).length;
  }

  getNumOfUncompletedTasksInInbox(): number {
    return this.list.filter((a) => 
      !a.project && !a.completed
    ).length;
  }

  getNumOfUncompletedTasksToday(): number {
    return this.getByDate(new Date()).length;
  }

  getTaskById(taskId: number): TaskTreeElem | undefined {
    return this.list.find((a) => a.id == taskId);
  }

  getTasksByProject(projectId: number): TaskTreeElem[] {
    return this.list.filter((a) => 
      a.project && a.project.id == projectId
    );
  }

  addNewTask(response: Task, project: ProjectTreeElem | undefined) {
    this.list.push({
      id:response.id,
      title: response.title,
      description: response.description,
      project: project ? project : null,
      parent: null,
      due: response.due ? new Date(response.due) : null,
      completed: false,
      order: response.projectOrder,
      realOrder: response.projectOrder, //todo
      hasChildren: false, 
      indent: 0, //todo
      parentList: [], 
      collapsed: false
    })
  }

  updateTask(response: Task, project: ProjectTreeElem | undefined) {
    let task = this.getTaskById(response.id);
    if(task) {
      task.description = response.description;
      task.title = response.title;
      task.project = project ? project : null;
      task.due = response.due ? new Date(response.due) : null;
    }
  }

  changeTaskCompletion(response: Task) {
    let task = this.getTaskById(response.id);
    if(task) {
      task.completed = response.completed;
    }
  }
}
