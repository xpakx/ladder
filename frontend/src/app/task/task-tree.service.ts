import { Injectable } from '@angular/core';
import { LabelDetails } from '../label/dto/label-details';
import { ParentWithId } from '../common/dto/parent-with-id';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { ProjectWithNameAndId } from 'src/app/project/dto/project-with-name-and-id';
import { Task } from './dto/task';
import { TaskDetails } from './dto/task-details';
import { TaskTreeElem } from './dto/task-tree-elem';
import { UserMin } from '../user/dto/user-min';
import { IndentableService } from 'src/app/common/indentable-service';
import { MovableTaskTreeService } from './movable-task-tree-service';

@Injectable({
  providedIn: 'root'
})
export class TaskTreeService extends IndentableService<ParentWithId> 
implements MovableTaskTreeService<Task, TaskTreeElem> {
  public list: TaskTreeElem[] = [];
  private lastArchivization: Date | undefined;
  protected id: number = -1;

  constructor() { super() }

  public getLastArchivization(): Date | undefined {
    return this.lastArchivization;
  }
  
  load(tasks: TaskDetails[]): void {
    this.id = Number(localStorage.getItem("user_id"));
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
      timeboxed: task.timeboxed,
      completed: task.completed,
      labels: task.labels,
      priority: task.priority,
      modifiedAt:  new Date(task.modifiedAt),
      assigned: task.assigned
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

  protected isTaskAssignedToMe(a: TaskTreeElem): boolean {
    return (!a.assigned || a.assigned.id == this.id);
  }

  getByDate(date: Date): TaskTreeElem[] {
    return this.list.filter((a) => 
      this.isTaskAssignedToMe(a) &&
      a.due && a.due.getDate() === date.getDate() && 
      a.due.getMonth() === date.getMonth() && 
      a.due.getFullYear() === date.getFullYear()
    );
  }

  getByDateUncompleted(date: Date): TaskTreeElem[] {
    return this.list.filter((a) => 
      this.isTaskAssignedToMe(a) &&
      !a.completed && 
      a.due && 
      a.due.getDate() === date.getDate() && 
      a.due.getMonth() === date.getMonth() && 
      a.due.getFullYear() === date.getFullYear() 
    );
  }

  getOverdue(dateVar: Date): TaskTreeElem[] {
    let date = new Date(dateVar);
    date.setHours(0);
    date.setMinutes(0);
    date.setSeconds(0);
  return this.list.filter((a) => 
    this.isTaskAssignedToMe(a) &&
    !a.completed && a.due && a.due < date
  );
  }

  getByDateBetween(date1: Date, date2: Date): TaskTreeElem[] {
    return this.list.filter((a) => 
      this.isTaskAssignedToMe(a) &&
      a.due && a.due > date1 && a.due < date2
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

  addNewTask(response: Task, project: ProjectTreeElem | undefined, indent: number = 0, parent: ParentWithId | null = null, labels: LabelDetails[] = []): void {
    this.list.push({
      id: response.id,
      title: response.title,
      description: response.description,
      project: project ? project : null,
      parent: parent,
      due: response.due ? new Date(response.due) : null,
      timeboxed: response.timeboxed,
      completed: false,
      order: response.projectOrder,
      realOrder: response.projectOrder,
      dailyOrder: response.dailyViewOrder,
      hasChildren: false, 
      indent: indent,
      parentList: [], 
      collapsed: false,
      labels: labels,
      priority: response.priority,
      modifiedAt:  new Date(response.modifiedAt),
      assigned: null
    });
    this.sort();
  }

  updateTask(response: Task, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []): void {
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
      task.timeboxed = response.timeboxed;
      task.dailyOrder = response.dailyViewOrder;
      task.priority = response.priority;
      task.labels = labels;
      task.modifiedAt = new Date(response.modifiedAt);
    }
  }

  changeTaskCompletion(response: Task): void {
    let task = this.getById(response.id);
    if(task) {
      task.completed = response.completed;
      task.modifiedAt =  new Date(response.modifiedAt);
      let username = localStorage.getItem('username');
      let id = Number(localStorage.getItem('user_id'))
      if(username && id) {
        task.assigned = {username: username, id: id};
      }
    }
  }


  moveAfter(task: Task, afterId: number, indent: number = 0): void {
    let afterTask = this.getById(afterId);
    let movedTask = this.getById(task.id);
    if(afterTask && movedTask) {
      let tas : TaskTreeElem = afterTask;
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getById(movedTask.parent.id) : undefined;
      this.incrementOrderAfter(tas);
      
      movedTask.indent = indent;
      movedTask.parent = afterTask.parent;
      movedTask.order = afterTask.order+1;
      movedTask.modifiedAt =  new Date(task.modifiedAt);

      this.recalculateChildrenIndent(movedTask.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(tas);

      this.sort();
    }
  }

  moveAsChild(task: Task, parentId: number, indent: number = 0): void {
    let parentTask = this.getById(parentId);
    let movedTask = this.getById(task.id);
    if(parentTask && movedTask) {
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getById(movedTask.parent.id) : undefined;
      this.incrementOrderForAllSiblings(parentTask);
      
      movedTask.indent = indent;
      movedTask.order = 1;
      movedTask.parent = parentTask;
      movedTask.modifiedAt =  new Date(task.modifiedAt);

      this.recalculateChildrenIndent(movedTask.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(parentTask);

      this.sort();
    }
  }

  moveAsFirst(task:Task): void {
    this.moveTaskAsFirst(task, undefined);
  }

  moveTaskAsFirst(task: Task, project: ProjectTreeElem | undefined): void {
    let movedTask = this.getById(task.id);
    if(movedTask) {
      let oldParent: TaskTreeElem | undefined = movedTask.parent ? this.getById(movedTask.parent.id) : undefined;
      this.incrementOrderForFirstOrderTasks(project);
      
      movedTask.indent = 0;
      movedTask.order = 1;
      movedTask.parent = null;
      movedTask.modifiedAt =  new Date(task.modifiedAt);

      this.recalculateChildrenIndent(movedTask.id, 1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }

      this.sort();
    }
  }

  deleteTask(taskId: number): void {
    let task = this.getById(taskId);
    let parent = task?.parent ? this.getById(task.parent.id) : undefined;
    let ids = this.getAllChildren(taskId);
    this.list = this.list.filter((a) => !ids.includes(a.id));
    if(parent) {
      this.recalculateHasChildren(parent);
    }
  }

  deleteAllTasksFromProject(projectId: number): void {
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

  updateTaskDate(task: Task): void {
    let taskToEdit =  this.getById(task.id);
    if(taskToEdit) {
      taskToEdit.due = task.due? new Date(task.due) : null;
      taskToEdit.timeboxed = task.timeboxed;
      taskToEdit.dailyOrder = task.dailyViewOrder;
      taskToEdit.modifiedAt =  new Date(task.modifiedAt);
    }
  }

  addNewTaskAfter(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []): void {
    let afterTask = this.getById(afterId);
    if(afterTask) {
      let tsk : TaskTreeElem = afterTask;
      task.projectOrder = tsk.order+1;
      this.incrementOrderAfter(tsk);
      this.addNewTask(task, project, indent, tsk.parent, labels);
    }
  }

  addNewTaskBefore(task: Task, indent: number, beforeId: number, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []): void {
    let beforeTask = this.getById(beforeId);
    if(beforeTask) {
      let tsk : TaskTreeElem = beforeTask;
      task.projectOrder = tsk.order;
      this.incrementOrderAfterOrEqual(tsk);
      this.addNewTask(task, project, indent, tsk.parent, labels);
    }
  }

  addNewTaskAsChild(task: Task, indent: number, parentId: number, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []): void {
    let parent = this.getById(parentId);
    if(parent) {
      let tsk : ParentWithId = { id: parent.id };
      this.addNewTask(task, project, indent, tsk, labels);
      this.recalculateHasChildren(parent);
      this.sort();
    }
  }

  moveTaskToProject(task: Task, project: ProjectTreeElem | undefined): void {
    let taskToMove = this.getById(task.id);
    if(taskToMove) {
      let tasks = project ? this.getTasksByProject(project.id) : this.getTasksFromInbox();
      if(!this.hasSameProject(taskToMove, project)) {
        this.updateChildrenProject(project, taskToMove);
        this.updateParentChildren(taskToMove);
      }
      taskToMove.modifiedAt =  new Date(task.modifiedAt);
      taskToMove.parent = null;
      taskToMove.indent = 0;
      taskToMove.project = project ? project : null;
      taskToMove.order = tasks
        .map((a) => a.order)
        .reduce(((total, curr)=>Math.max(total, curr)), 0) + 1;
      this.sort();
    }
  }

  private updateParentChildren(task: TaskTreeElem): void {
    if(!task.parent) {return;}
    let parent = this.getById(task.parent.id);
    task.parent = null;
    if(parent) {
      this.recalculateHasChildren(parent);
    }
  }

  private updateChildrenProject(project: ProjectTreeElem | undefined, parent: TaskTreeElem): void {
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

  private updateTaskChildren(children: TaskTreeElem, parent: TaskTreeElem, project: ProjectWithNameAndId| null): void {
    children.project = project;
    children.indent = children.indent - parent.indent;
  }

  private toProjectWithNameAndId(project: ProjectTreeElem): ProjectWithNameAndId {
    return {name: project.name, id: project.id};
  }

  private getImminentChildren(parentList: TaskTreeElem[], tasksForProject: TaskTreeElem[]): TaskTreeElem[] {
    let ids = parentList.map((a) => a.id);
    return tasksForProject
            .filter((a) => a.parent && ids.includes(a.parent.id));
  }

  private getTasksForProjectOrInbox(task: TaskTreeElem): TaskTreeElem[] {
    return task.project ? this.getTasksByProject(task.project.id) :
            this.getTasksFromInbox();
  }

  private hasSameProject(task: TaskTreeElem, project: ProjectTreeElem | undefined): boolean {
      return (task.project && project && task.project.id == project.id) || (!task.project && !project);
  }

  updateTaskPriority(task: Task): void {
    let taskToUpdate = this.getById(task.id);
    if(taskToUpdate) {
      taskToUpdate.priority = task.priority;
      taskToUpdate.modifiedAt =  new Date(task.modifiedAt);
    }
  }

  updateTaskLabels(task: Task, labels: LabelDetails[]): void {
    let taskToUpdate = this.getById(task.id);
    if(taskToUpdate) {
      taskToUpdate.labels = labels;
      taskToUpdate.modifiedAt =  new Date(task.modifiedAt);
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

  moveAsFirstDaily(task: Task): void {
    let movedTask = this.getById(task.id);
    if(movedTask) {
      let date = new Date(task.due);
      this.incrementDailyOrderForDate(date);
      movedTask.dailyOrder = 1;
      movedTask.due = date
      movedTask.modifiedAt =  new Date(task.modifiedAt);
    }
  }

  private incrementDailyOrderForDate(date: Date): void {
    let tasks = this.getByDate(date);
    for (let task of tasks) {
      task.dailyOrder = task.dailyOrder + 1;
    }
  }

  moveAfterDaily(task: Task, afterId: number): void {
    let afterTask = this.getById(afterId);
    let movedTask = this.getById(task.id);
    if(afterTask && movedTask) {
      let date = new Date(task.due);
      this.incrementDailyOrderForDateAfter(date, afterTask);
      movedTask.dailyOrder = afterTask.dailyOrder+1;
      movedTask.due = new Date(task.due);
      movedTask.modifiedAt = new Date(task.modifiedAt);
    }
  }

  private incrementDailyOrderForDateAfter(date: Date, afterTask: TaskTreeElem): void {
    let tasks = this.getByDate(date)
      .filter((a) => a.dailyOrder > afterTask.dailyOrder);
    for (let task of tasks) {
      task.dailyOrder = task.dailyOrder + 1;
    }
  }

  addDuplicated(response: TaskDetails[], mainId: number | undefined = undefined): void {
    let tasks = this.transformAll(response);
    let mainTask = mainId ? this.getById(mainId) : undefined;
    if(mainTask) {
      this.incrementOrderAfter(mainTask);
      if(mainTask.due) {
        this.incrementDailyOrderForDateAfter(mainTask.due, mainTask);
      }
    }
    this.list = this.list.concat(tasks);
    this.sort();
  }

  private incrementOrderForFirstOrderTasks(project: ProjectTreeElem | undefined): void {
    let tasks = this.list
      .filter((a) => project ? a.project && a.project.id == project.id : !a.project)
      .filter((a) => !a.parent);
    for (let pro of tasks) {
      pro.order = pro.order + 1;
    }
  }

  incrementOrderForAllSiblings(parent: TaskTreeElem): void {
    let siblings = this.list
        .filter((a) => !a.parent && !parent || (a.parent && parent && a.parent.id == parent.id));
    for(let sibling of siblings) {
      sibling.order = sibling.order + 1;
    }
  }
  
  incrementOrderAfter(task: TaskTreeElem): void {
    let siblingsAfter = this.list
        .filter((a) => !a.parent && !task.parent || (a.parent && task.parent && a.parent.id == task.parent.id))
        .filter((a) => a.order > task.order);
    for(let sibling of siblingsAfter) {
      sibling.order = sibling.order + 1;
    }
  }

  incrementOrderAfterOrEqual(task: TaskTreeElem): void {
    let siblingsAfter = this.list
        .filter((a) => !a.parent && !task.parent || (a.parent && task.parent && a.parent.id == task.parent.id))
        .filter((a) => a.order >= task.order);
    for(let sibling of siblingsAfter) {
      sibling.order = sibling.order + 1;
    }
  }

  getByParentId(parentId: number): TaskTreeElem[] {
    return this.list.filter((a) => a.parent && a.parent.id == parentId);
  }

  sync(tasks: TaskDetails[]): void {
    for(let task of tasks) {
      let taskWithId = this.getById(task.id);
      if(taskWithId) {
        if(task.archived) {
          this.lastArchivization = new Date(task.modifiedAt);
          this.deleteTask(task.id)
        }
        else {
        this.updateTaskDetails(taskWithId, task, tasks);
        }
      } else if (!task.archived) {
        this.list.push(this.transformSync(task, tasks));
      } else {
        this.lastArchivization = new Date(task.modifiedAt);
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
      timeboxed: task.timeboxed,
      completed: task.completed,
      labels: task.labels,
      priority: task.priority,
      modifiedAt: task.modifiedAt,
      assigned: task.assigned
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

  updateTaskDetails(task: TaskTreeElem, response: TaskDetails, tasks: TaskDetails[]): void {
    task.description = response.description;
    task.title = response.title;
    let oldParent = task.parent ? this.getById(task.parent.id) : null;
    let newParent = response.parent ? this.getById(response.parent.id) : null;
    task.project = response.project;
    task.priority = response.priority
    task.parent = response.parent;
    task.due = response.due ? new Date(response.due) : null;
    task.timeboxed = response.timeboxed;
    task.completed = response.completed;
    task.collapsed = response.collapsed;
    task.dailyOrder = response.dailyViewOrder;
    task.order = response.projectOrder;
    task.priority = response.priority;
    task.labels = response.labels;
    task.modifiedAt = new Date(response.modifiedAt);
    task.indent = newParent ? newParent.indent+1 : 0;
    task.parentList = [];
    task.assigned = response.assigned;
    this.recalculateChildrenIndent(task.id, task.indent+1);
    if(oldParent) {
      this.recalculateHasChildrenSync(oldParent, tasks);
    }
    this.recalculateHasChildrenSync(task, tasks);
  }

  private howManyChildrenSync(parentId: number, tasks: TaskDetails[]): number {
    let syncChildren = tasks.filter((a) => a.parent && a.parent.id == parentId);
    let ids = syncChildren.map((a) => a.id);
    let children = this.list.filter((a) => a.parent && a.parent.id == parentId && !ids.includes(a.id));
    return children.length + syncChildren.length;
  }

  private recalculateHasChildrenSync(task: TaskTreeElem, tasks: TaskDetails[]): void {
      let children = this.howManyChildrenSync(task.id, tasks);
      task.hasChildren = children > 0 ? true : false;
      for(let parent of task.parentList) {
          let parentChildren = this.howManyChildrenSync(parent.id, tasks);
          parent.hasChildren = parentChildren > 0 ? true : false;
      }
  }

  transformAndReturn(tasks: TaskDetails[]): TaskTreeElem[]  {
    let list = tasks.map((a) => this.transform(a, tasks));
    this.sortToReturn(list);
    return list;
  }

  protected sortToReturn(tasks: TaskTreeElem[]): void {
    tasks.sort((a, b) => a.order - b.order);
    this.calculateRealOrderToReturn(tasks);
    tasks.sort((a, b) => a.realOrder - b.realOrder);
  }

  private calculateRealOrderToReturn(tasks: TaskTreeElem[]): void {
    let tsks = tasks.filter((a) => a.indent == 0);
    var offset = 0;
    for(let task of tsks) {
        task.parentList = [];
        offset += this.countAllChildrenToReturn(task, offset, tasks) +1;
    }
  }

  private countAllChildrenToReturn(task: TaskTreeElem, offset: number, tasks: TaskTreeElem[], parent?: TaskTreeElem ): number {
    task.realOrder = offset;
    offset += 1;
    
    if(parent) {
        task.parentList = [...parent.parentList];
        task.parentList.push(parent);
    }

    if(!task.hasChildren) {
        return 0;
    }

    let children = tasks.filter((a) => a.parent?.id == task.id);
    var num = 0;
    for(let proj of children) {
        let childNum = this.countAllChildrenToReturn(proj, offset, tasks, task);
        num += childNum+1;
        offset += childNum+1;      
    } 
    return num;
  }

  restoreTask(task: Task, tree: TaskTreeElem[]): void {
    let newTask = tree.find((a) => a.id == task.id);
    let oldTask = this.getById(task.id);
    if(newTask && !oldTask) {
      newTask.parent = null; 
      newTask.order = task.projectOrder;
      newTask.dailyOrder = task.dailyViewOrder;
      newTask.modifiedAt = new Date(task.modifiedAt);
      this.list.push(newTask);
      let children = [newTask];
      while(children.length > 0) {
        let ids = children.map((a) => a.id);
        children = tree.filter((a) => a.parent && ids.includes(a.parent.id));
        for(let child of children) {
          child.modifiedAt = task.modifiedAt;
          this.list.push(child);
        }
      }
      this.sort();
    }
  }

  archiveTask(task: Task): void {
    this.lastArchivization = new Date(task.modifiedAt);
    this.deleteTask(task.id);
  }

  updateTasksDate(tasks: Task[]): void {
    for(let task of tasks) {
      this.updateTaskDate(task);
    }
  }

  collapse(response: Task): void {
    let task = this.getById(response.id);
    if(task){
      task.collapsed = response.collapsed;
      task.modifiedAt = new Date(response.modifiedAt);
    }
  }

  updateAssignation(response: Task, user: UserMin): void {
    let task = this.getById(response.id);
    if(task){
      task.assigned = user;
      task.modifiedAt = new Date(response.modifiedAt);
    }
  }

  deleteAssignations(projectId: number, userId: number): void {
    this.list
      .filter((a) => a.project && a.project.id == projectId)
      .filter((a) => a.assigned && a.assigned.id == userId)
      .forEach((a) => a.assigned = null);
  }
}
