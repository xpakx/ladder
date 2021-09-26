import { Injectable } from '@angular/core';
import { LabelDetails } from '../entity/label-details';
import { Project } from '../entity/project';
import { ProjectDetails } from '../entity/project-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { ProjectWithNameAndId } from '../entity/project-with-name-and-id';
import { Task } from '../entity/task';
import { TaskDetails } from '../entity/task-details';
import { TaskTreeElem } from '../entity/task-tree-elem';
import { UserWithData } from '../entity/user-with-data';
import { ProjectTreeService } from './project-tree.service';

@Injectable({
  providedIn: 'root'
})
export class TreeService {
  public tasks: TaskTreeElem[] = [];
  public labels: LabelDetails[] = [];
  public loaded: boolean = false;
  public projectCollapsed: boolean = true;
  
  constructor(private projects: ProjectTreeService) { }

  isLoaded(): boolean {
    return this.loaded;
  }

  load(tree: UserWithData) {
    this.loaded = true;
    this.projectCollapsed = tree.projectCollapsed;
    this.projects.load(tree.projects);

    this.tasks = this.transformAllTasks(tree.tasks);
    this.tasks.sort((a, b) => a.order - b.order);
    this.calculateRealOrderForTasks();
    this.tasks.sort((a, b) => a.realOrder - b.realOrder);
    this.labels = tree.labels;
  }

  getProjects() {
    return this.projects.projectList;
  }

  addNewProject(project: Project, indent: number, parent: ProjectWithNameAndId | null = null) {
    this.projects.addNewProject(project, indent, parent);
  }

  addNewProjectAfter(project: Project, indent: number, afterId: number) {
    this.projects.addNewProjectAfter(project, indent, afterId);
  }

  moveProjectAfter(project: Project, indent: number, afterId: number) {
    this.projects.moveProjectAfter(project, indent, afterId);
  }

  moveProjectAsChild(project: Project, indent: number, parentId: number) {
    this.projects.moveProjectAsChild(project, indent, parentId);
  }

  addNewProjectBefore(project: Project, indent: number, beforeId: number) {
    this.projects.addNewProjectBefore(project, indent, beforeId);
  }
  
  updateProject(project: Project, id: number) {
    this.projects.updateProject(project, id);
  }

  getProjectById(projectId: number): ProjectTreeElem | undefined {
    return this.projects.getProjectById(projectId);
  }

  deleteProject(projectId: number) {
    this.projects.deleteProject(projectId);
  }

  changeFav(response: Project) {
    this.projects.changeFav(response);
  }

  filterProjects(text: string): ProjectTreeElem[] {
    return this.projects.filterProjects(text);
  }






  transformAllTasks(tasks: TaskDetails[]):  TaskTreeElem[] {
    return tasks.map((a) => this.transformTask(a, tasks));
  }

  transformTask(task: TaskDetails, tasks: TaskDetails[]): TaskTreeElem {
    let indent: number = this.getTaskIndent(task.id, tasks);
    return {
      id: task.id,
      title: task.title,
      parent: task.parent,
      order: task.projectOrder,
      realOrder: task.projectOrder,
      hasChildren: this.hasChildrenByTaskId(task.id, tasks),
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

  hasChildrenByTaskId(taskId: number, tasks: TaskDetails[]): boolean {
    return tasks.find((a) => a.parent?.id == taskId) != null;
  }

  calculateRealOrderForTasks() {
    let tsk = this.tasks.filter((a) => a.indent == 0);
    var offset = 0;
    for(let task of tsk) {
      task.parentList = [];
      offset += this.countAllTaskChildren(task, offset) +1;
    }
  }

  countAllTaskChildren(task: TaskTreeElem, offset: number, parent?: TaskTreeElem): number {
    task.realOrder = offset;
    offset += 1;
    
    if(parent) {
      task.parentList = [...parent.parentList];
      task.parentList.push(parent);
    }

    if(!task.hasChildren) {
      return 0;
    }

    let children = this.tasks.filter((a) => a.parent?.id == task.id);
    var num = 0;
    for(let tsk of children) {
      let childNum = this.countAllTaskChildren(tsk, offset, task);
      num += childNum+1;
      offset += childNum+1;      
    } 
    return num;
  }
  
  getByDate(date: Date): TaskTreeElem[] {
    return this.tasks.filter((a) => 
      a.due && a.due.getDate() === date.getDate() && a.due.getMonth() === date.getMonth() && a.due.getFullYear() === date.getFullYear() 
    );
  }

  getNumOfUncompletedTasksByProject(projectId: number): number {
    return this.tasks.filter((a) => 
      a.project && a.project.id == projectId && !a.completed
    ).length;
  }

  getNumOfUncompletedTasksInInbox(): number {
    return this.tasks.filter((a) => 
      !a.project && !a.completed
    ).length;
  }

  getNumOfUncompletedTasksToday(): number {
    return this.getByDate(new Date()).length;
  }

  getTaskById(taskId: number): TaskTreeElem | undefined {
    return this.tasks.find((a) => a.id == taskId);
  }

  getTasksByProject(projectId: number): TaskTreeElem[] {
    return this.tasks.filter((a) => 
      a.project && a.project.id == projectId
    );
  }


  addNewTask(response: Task, projectId: number | undefined) {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.push({
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

  updateTask(response: Task, projectId: number | undefined) {
    let task = this.getTaskById(response.id);
    if(task) {
      let project = projectId ? this.getProjectById(projectId) : undefined;
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
