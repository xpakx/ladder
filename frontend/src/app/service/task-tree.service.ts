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
      completed: task.completed,
      labels: task.labels,
      priority: task.priority
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

  getTasksFromInbox(): TaskTreeElem[] {
    return this.list.filter((a) => 
      !a.project
    );
  }

  addNewTask(response: Task, project: ProjectTreeElem | undefined, indent: number = 0, parent: ParentWithId | null = null) {
    this.list.push({
      id:response.id,
      title: response.title,
      description: response.description,
      project: project ? project : null,
      parent: parent,
      due: response.due ? new Date(response.due) : null,
      completed: false,
      order: response.projectOrder,
      realOrder: response.projectOrder, //todo
      hasChildren: false, 
      indent: indent,
      parentList: [], 
      collapsed: false,
      labels: [],
      priority: response.priority
    });
    this.sort();
  }

  updateTask(response: Task, project: ProjectTreeElem | undefined) {
    let task = this.getTaskById(response.id);
    if(task) {
      task.description = response.description;
      task.title = response.title;
      task.project = project ? project : null;
      task.due = response.due ? new Date(response.due) : null;
      task.priority = response.priority;
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

      this.recalculateChildrenIndent(movedTask.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(tas);

      this.sort();
    }
  }

  moveTaskAsFirst(task: Task, project: ProjectTreeElem | undefined) {
    let movedTask = this.getTaskById(task.id);
    if(movedTask) {
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getTaskById(movedTask.parent.id) : undefined;
      let tasks = this.list
        .filter((a) => project ? a.project && a.project.id == project.id : !a.project)
        .filter((a) => !a.parent);
      for(let pro of tasks) {
        pro.order = pro.order + 1;
      }
      
      movedTask.indent = 0;
      movedTask.order = 1;
      movedTask.parent = null;

      this.recalculateChildrenIndent(movedTask.id, 2);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }

      this.sort();
    }
  }

  deleteTask(taskId: number) {
    this.list = this.list.filter((a) => a.id != taskId);
  }

  updateTaskDate(task: Task) {
    let taskToEdit =  this.getTaskById(task.id);
    if(taskToEdit) {
      taskToEdit.due = task.due? new Date(task.due) : null;
    }
  }

  addNewTaskAfter(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined) {
    let afterTask = this.getTaskById(afterId);
    if(afterTask) {
      let tsk : TaskTreeElem = afterTask;
      task.projectOrder = tsk.order+1;
      let tasks = this.list
        .filter((a) => project ? a.project && a.project.id == project.id : !a.project)
        .filter((a) => a.parent == tsk.parent)
        .filter((a) => a.order > tsk.order);
      for(let sibling of tasks) {
        sibling.order = sibling.order + 1;
      }
      this.addNewTask(task, project, indent, tsk.parent);
    }
  }

  addNewTaskBefore(task: Task, indent: number, beforeId: number, project: ProjectTreeElem | undefined) {
    let beforeTask = this.getTaskById(beforeId);
    if(beforeTask) {
      let tsk : TaskTreeElem = beforeTask;
      task.projectOrder = tsk.order;
      let tasks = this.list
        .filter((a) => project ? a.project && a.project.id == project.id : !a.project)
        .filter((a) => a.parent == tsk.parent)
        .filter((a) => a.order >= tsk.order);
      for(let sibling of tasks) {
        sibling.order = sibling.order + 1;
      }
      this.addNewTask(task, project, indent, tsk.parent);
    }
  }

  moveTaskToProject(task: Task, project: ProjectTreeElem | undefined) {
    let taskToMove = this.getTaskById(task.id);
    if(taskToMove) {
      let tasks = project ? this.getTasksByProject(project.id) : this.getTasksFromInbox();
      taskToMove.project = project ? project : null;
      taskToMove.order = tasks
        .map((a) => a.order)
        .reduce(((total, curr)=>Math.max(total, curr)), 0) + 1;
      this.sort();
    }
  }
}
