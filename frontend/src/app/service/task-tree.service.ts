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
      collapsed: task.collapsed,
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

  getByDateUncompleted(date: Date): TaskTreeElem[] {
    return this.list.filter((a) => 
      !a.completed && a.due && a.due.getDate() === date.getDate() && a.due.getMonth() === date.getMonth() && a.due.getFullYear() === date.getFullYear() 
    );
  }

  getNumOfUncompletedTasksByProject(projectId: number): number {
    return this.list.filter((a) => 
      a.project && a.project.id == projectId && !a.completed
    ).length;
  }

  getNumOfUncompletedTasksByParent(taskId: number): number {
    return this.list.filter((a) => 
      a.parent && a.parent.id == taskId && !a.completed
    ).length;
  }

  getNumOfTasksByParent(taskId: number): number {
    return this.list.filter((a) => 
      a.parent && a.parent.id == taskId
    ).length;
  }

  getNumOfUncompletedTasksInInbox(): number {
    return this.list.filter((a) => 
      !a.project && !a.completed
    ).length;
  }

  getNumOfUncompletedTasksToday(): number {
    return this.getByDateUncompleted(new Date()).length;
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

  moveTaskAfter(task: Task, indent: number, afterId: number) {
    let afterTask = this.getTaskById(afterId);
    let movedTask = this.getTaskById(task.id);
    if(afterTask && movedTask) {
      let tas : TaskTreeElem = afterTask;
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getTaskById(movedTask.parent.id) : undefined;
      let tasks = this.list
        .filter((a) => a.parent == tas.parent)
        .filter((a) => a.order > tas.order);
        for(let tsk of tasks) {
          tsk.order = tsk.order + 1;
        }
      
      movedTask.indent = indent;
      movedTask.parent = afterTask.parent;
      movedTask.order = afterTask.order+1;

      this.recalculateChildrenIndent(movedTask.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(tas);

      this.sort();
      console.log(JSON.stringify(movedTask, undefined, 2));
    }
  }

  moveTaskAsChild(task: Task, indent: number, parentId: number) {
    let parentTask = this.getTaskById(parentId);
    let movedTask = this.getTaskById(task.id);
    if(parentTask && movedTask) {
      let tas : TaskTreeElem = parentTask;
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getTaskById(movedTask.parent.id) : undefined;
      let tasks = this.list
        .filter((a) => a.parent == tas);
        for(let tsk of tasks) {
          tsk.order = tsk.order + 1;
        }
      
      movedTask.indent = indent;
      movedTask.order = 1;
      movedTask.parent = parentTask;

      console.log("My id: " + movedTask.id + ", my parent id: " + (movedTask.parent ? movedTask.parent.id : "null"))

      this.recalculateChildrenIndent(movedTask.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(tas);

      this.sort();
      console.log(JSON.stringify(movedTask, undefined, 2));
    }
  }
}
