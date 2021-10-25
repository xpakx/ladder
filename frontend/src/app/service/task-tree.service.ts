import { Injectable } from '@angular/core';
import { LabelDetails } from '../entity/label-details';
import { ParentWithId } from '../entity/parent-with-id';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { ProjectWithNameAndId } from '../entity/project-with-name-and-id';
import { Task } from '../entity/task';
import { TaskDetails } from '../entity/task-details';
import { TaskTreeElem } from '../entity/task-tree-elem';
import { IndentableService } from './indentable-service';
import { MovableTaskTreeService } from './movable-task-tree-service';

@Injectable({
  providedIn: 'root'
})
export class TaskTreeService extends IndentableService<ParentWithId> 
implements MovableTaskTreeService<Task, TaskTreeElem> {
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
      dailyOrder: task.dailyViewOrder,
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

  getNumOfCompletedTasksByParent(taskId: number): number {
    return this.list.filter((a) => 
      a.parent && a.parent.id == taskId && a.completed
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

  getById(taskId: number): TaskTreeElem | undefined {
    return this.list.find((a) => a.id == taskId);
  }

  getAll(): TaskTreeElem[] {
    return this.list;
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

  addNewTask(response: Task, project: ProjectTreeElem | undefined, indent: number = 0, parent: ParentWithId | null = null, labels: LabelDetails[] = []) {
    this.list.push({
      id:response.id,
      title: response.title,
      description: response.description,
      project: project ? project : null,
      parent: parent,
      due: response.due ? new Date(response.due) : null,
      completed: false,
      order: response.projectOrder,
      realOrder: response.projectOrder,
      dailyOrder: response.dailyViewOrder, //todo
      hasChildren: false, 
      indent: indent,
      parentList: [], 
      collapsed: false,
      labels: labels,
      priority: response.priority
    });
    this.sort();
  }

  updateTask(response: Task, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []) {
    let task = this.getById(response.id);
    if(task) {
      task.description = response.description;
      task.title = response.title;
      if(!this.hasSameProject(task, project)) {
        this.updateChildrenProject(project, task);
        task.parent = null;
        task.indent = 0;
      }
      task.project = project ? project : null;
      task.due = response.due ? new Date(response.due) : null;
      task.dailyOrder = response.dailyViewOrder;
      task.priority = response.priority;
      task.labels = labels
    }
  }

  changeTaskCompletion(response: Task) {
    let task = this.getById(response.id);
    if(task) {
      task.completed = response.completed;
    }
  }


  moveAfter(task: Task, afterId: number, indent: number = 0) {
    let afterTask = this.getById(afterId);
    let movedTask = this.getById(task.id);
    if(afterTask && movedTask) {
      let tas : TaskTreeElem = afterTask;
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getById(movedTask.parent.id) : undefined;
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

  moveAsChild(task: Task, parentId: number, indent: number = 0) {
    let parentTask = this.getById(parentId);
    let movedTask = this.getById(task.id);
    if(parentTask && movedTask) {
      let tas : TaskTreeElem = parentTask;
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getById(movedTask.parent.id) : undefined;
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

  moveAsFirst(task:Task) {
    this.moveTaskAsFirst(task, undefined);
  }

  moveTaskAsFirst(task: Task, project: ProjectTreeElem | undefined) {
    let movedTask = this.getById(task.id);
    if(movedTask) {
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getById(movedTask.parent.id) : undefined;
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
    let ids = this.getAllChildren(taskId);
    this.list = this.list.filter((a) => !ids.includes(a.id));
  }

  deleteAllTasksFromProject(projectId: number) {
    this.list = this.list.filter((a) => !a.project || a.project.id != projectId);
  }

  protected getAllChildren(taskId: number): number[] {
    let children = this.list
    .filter((a) => a.parent && a.parent.id == taskId);
    let result: number[] = [];
    result.push(taskId);
    for(let child of children) {
        result = result.concat(this.getAllChildren(child.id));
    }
    return result;
  }

  updateTaskDate(task: Task) {
    let taskToEdit =  this.getById(task.id);
    if(taskToEdit) {
      taskToEdit.due = task.due? new Date(task.due) : null;
      taskToEdit.dailyOrder = task.dailyViewOrder;
    }
  }

  addNewTaskAfter(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []) {
    let afterTask = this.getById(afterId);
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
      this.addNewTask(task, project, indent, tsk.parent, labels);
    }
  }

  addNewTaskBefore(task: Task, indent: number, beforeId: number, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []) {
    let beforeTask = this.getById(beforeId);
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
      this.addNewTask(task, project, indent, tsk.parent, labels);
    }
  }

  addNewTaskAsChild(task: Task, indent: number, parentId: number, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []) {
    let parent = this.getById(parentId);
    if(parent) {
      let tsk : TaskTreeElem = parent;
      task.projectOrder = tsk.order;
      this.addNewTask(task, project, indent, tsk, labels);
    }
  }

  moveTaskToProject(task: Task, project: ProjectTreeElem | undefined) {
    let taskToMove = this.getById(task.id);
    if(taskToMove) {
      let tasks = project ? this.getTasksByProject(project.id) : this.getTasksFromInbox();
      if(!this.hasSameProject(taskToMove, project)) {
        this.updateChildrenProject(project, taskToMove);
        this.updateParentChildren(taskToMove);
      }
      taskToMove.parent = null;
      taskToMove.indent = 0;
      taskToMove.project = project ? project : null;
      taskToMove.order = tasks
        .map((a) => a.order)
        .reduce(((total, curr)=>Math.max(total, curr)), 0) + 1;
      this.sort();
    }
  }

  private updateParentChildren(task: TaskTreeElem) {
    if(!task.parent) {return;}
    let parent = this.getById(task.parent.id);
    task.parent = null;
    if(parent) {
      this.recalculateHasChildren(parent);
    }
  }

  private updateChildrenProject(project: ProjectTreeElem | undefined, parent: TaskTreeElem) {
    let tasksForProject = this.getTasksForProjectOrInbox(parent)
            .filter((a) => a.parent != null);
    let children = this.getImminentChildren([parent], tasksForProject);
    let toUpdate: TaskTreeElem[] = [];
    while(children.length > 0) {
        let newProject = project ? this.toProjectWithNameAndId(project) : null;
        children.forEach((a) => this.updateTaskChildren(a, parent, newProject));
        toUpdate = toUpdate.concat(children);
        children = this.getImminentChildren(children, tasksForProject);
    }
  }

  private updateTaskChildren(children: TaskTreeElem, parent: TaskTreeElem, project: ProjectWithNameAndId| null) {
    children.project = project;
    children.indent = children.indent - parent.indent;
  }

  private toProjectWithNameAndId(project: ProjectTreeElem): ProjectWithNameAndId {
    return {name: project.name, id: project.id};
  }

  private getImminentChildren(parentList: TaskTreeElem[], tasksForProject: TaskTreeElem[]) {
    let ids = parentList.map((a) => a.id);
    return tasksForProject
            .filter((a) => a.parent && ids.includes(a.parent.id));
  }

  private getTasksForProjectOrInbox(task: TaskTreeElem) {
    return task.project ? this.getTasksByProject(task.project.id) :
            this.getTasksFromInbox();
  }

  private hasSameProject(task: TaskTreeElem, project: ProjectTreeElem | undefined): boolean {
      return (task.project && project && task.project.id == project.id) || (!task.project && !project);
  }

  updateTaskPriority(task: Task) {
    let taskToUpdate = this.getById(task.id);
    if(taskToUpdate) {
      taskToUpdate.priority = task.priority;
    }
  }

  updateTaskLabels(task: Task, labels: LabelDetails[]) {
    let taskToUpdate = this.getById(task.id);
    if(taskToUpdate) {
      taskToUpdate.labels = labels;
    }
  }

  getNumOfUncompletedTasksByLabel(labelId: number): number {
    return this.list.filter((a) => 
      !a.completed && a.labels.find((b) => b.id == labelId)
    ).length;
  }

  getTasksByLabel(id: number): TaskTreeElem[] {
    return this.list.filter((a) => 
      !a.completed && a.labels.find((b) => b.id == id)
    );
  }


  moveAsFirstDaily(task: Task) {
    let movedTask = this.getById(task.id);
    if(movedTask) {
      let tasks = this.getByDate(new Date);
        
      for(let pro of tasks) {
        pro.dailyOrder = pro.dailyOrder + 1;
      }
      
      movedTask.dailyOrder = 1;
    }
  }

  moveAfterDaily(task: Task, afterId: number) {
    let afterTask = this.getById(afterId);
    let movedTask = this.getById(task.id);
    if(afterTask && movedTask) {
      let tas : TaskTreeElem = afterTask;
      let tasks = this.getByDate(new Date)
        .filter((a) => a.dailyOrder > tas.dailyOrder);
        for(let tsk of tasks) {
          tsk.dailyOrder = tsk.dailyOrder + 1;
        }
      
      movedTask.dailyOrder = afterTask.dailyOrder+1;
    }
  }

  addDuplicated(response: TaskDetails[]) {
    let tasks = this.transformAll(response);
    this.list = this.list.concat(tasks);
    this.sort();
  }

  getByParentId(parentId: number): TaskTreeElem[] {
    return this.list.filter((a) => a.parent && a.parent.id == parentId);
  }

  sync(tasks: TaskDetails[]) {
    for(let task of tasks) {
      let taskWithId = this.getById(task.id);
      if(taskWithId) {
        this.updateTaskDetails(taskWithId, task);
      } else {
        this.list.push(this.transformSync(task, tasks));
      }
    }
    this.sort();
  }

  transformSync(task: TaskDetails, tasks: TaskDetails[]): TaskTreeElem {
    let indent: number = this.getIndentSync(task.id, tasks);
    return {
      id: task.id,
      title: task.title,
      parent: task.parent,
      order: task.projectOrder,
      realOrder: task.projectOrder,
      dailyOrder: task.dailyViewOrder,
      hasChildren: this.hasChildrenByIdSync(task.id, tasks),
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

  private hasChildrenByIdSync(taskId: number, tasks: TaskDetails[]): boolean {
    return this.hasChildrenById(taskId, tasks) || this.list.find((a) => a.parent?.id == taskId) != null;
  }

  private findParentByIdSync(taskId: number, tasks: TaskDetails[]): number | undefined {
    let syncDataId = tasks.find((a) => a.id == taskId)?.parent?.id;
    if(syncDataId) {return syncDataId;}
    return this.list.find((a) => a.id == taskId)?.parent?.id;
  }

  private getIndentSync(taskId: number, tasks: TaskDetails[]): number {
    let parentId: number | undefined = this.findParentByIdSync(taskId, tasks);
    let counter = 0;
    while(parentId != null) {
      counter +=1;
      parentId = this.findParentByIdSync(parentId, tasks);
    }
    return counter;
  }

  updateTaskDetails(task: TaskTreeElem, response: TaskDetails) {
    task.description = response.description;
    task.title = response.title;
    let oldParent = task.parent ? this.getById(task.parent.id) : null;
    let newParent = response.parent ? this.getById(response.parent.id) : null;
    task.project = response.project;
    task.priority = response.priority
    task.parent = response.parent;
    task.due = response.due ? new Date(response.due) : null;
    task.completed = response.completed;
    task.collapsed = response.collapsed;
    task.dailyOrder = response.dailyViewOrder;
    task.order = response.projectOrder;
    task.priority = response.priority;
    task.labels = response.labels;
    task.indent = newParent ? newParent.indent+1 : 0;
    this.recalculateChildrenIndent(task.id, task.indent+1);
    if(oldParent) {
      this.recalculateHasChildren(oldParent);
    }
    this.recalculateHasChildren(task);
  }
}
