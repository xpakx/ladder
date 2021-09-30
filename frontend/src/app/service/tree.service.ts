import { Injectable } from '@angular/core';
import { Label } from '../entity/label';
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
import { TaskTreeService } from './task-tree.service';

@Injectable({
  providedIn: 'root'
})
export class TreeService {
  public labels: LabelDetails[] = [];
  public loaded: boolean = false;
  public projectCollapsed: boolean = true;
  public labelCollapsed: boolean = true;
  
  constructor(private projects: ProjectTreeService, private tasks: TaskTreeService) { }

  isLoaded(): boolean {
    return this.loaded;
  }

  load(tree: UserWithData) {
    this.loaded = true;
    this.projectCollapsed = tree.projectCollapsed;
    this.projects.load(tree.projects);
    this.tasks.load(tree.tasks);
    this.labels = tree.labels;
  }

  getProjects() {
    return this.projects.list;
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

  moveProjectAsFirst(project: Project) {
    this.projects.moveProjectAsFirst(project);
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

  getTasks() {
    return this.tasks.list;
  }
  
  getByDate(date: Date): TaskTreeElem[] {
    return this.tasks.getByDate(date);
  }

  getNumOfUncompletedTasksByProject(projectId: number): number {
    return this.tasks.getNumOfUncompletedTasksByProject(projectId);
  }

  getNumOfUncompletedTasksByParent(parentId: number): number {
    return this.tasks.getNumOfUncompletedTasksByParent(parentId);
  }

  getNumOfTasksByParent(parentId: number): number {
    return this.tasks.getNumOfTasksByParent(parentId);
  }

  getNumOfUncompletedTasksInInbox(): number {
    return this.tasks.getNumOfUncompletedTasksInInbox();
  }

  getNumOfUncompletedTasksToday(): number {
    return this.tasks.getNumOfUncompletedTasksToday();
  }

  getTaskById(taskId: number): TaskTreeElem | undefined {
    return this.tasks.getTaskById(taskId);
  }

  getTasksByProject(projectId: number): TaskTreeElem[] {
    return this.tasks.getTasksByProject(projectId);
  }

  addNewTask(response: Task, projectId: number | undefined) {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.addNewTask(response, project);
  }

  updateTask(response: Task, projectId: number | undefined) {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.updateTask(response, project);
  }

  changeTaskCompletion(response: Task) {
    this.tasks.changeTaskCompletion(response);
  }

  moveTaskAfter(task: Task, indent: number, afterId: number) {
    this.tasks.moveTaskAfter(task, indent, afterId);
  }

  moveTaskAsChild(task: Task, indent: number, parentId: number) {
    this.tasks.moveTaskAsChild(task, indent, parentId);
  }

  moveTaskAsFirst(task: Task, project: ProjectTreeElem | undefined) {
    this.tasks.moveTaskAsFirst(task, project);
  }

  deleteTask(taskId: number) {
    this.tasks.deleteTask(taskId);
  }

  updateTaskDate(task: Task) {
    this.tasks.updateTaskDate(task);
  }

  addNewTaskAfter(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined) {
    this.tasks.addNewTaskAfter(task, indent, afterId, project);
  }

  addNewTaskBefore(task: Task, indent: number, beforeId: number, project: ProjectTreeElem | undefined) {
    this.tasks.addNewTaskBefore(task, indent, beforeId, project);
  }

  moveTaskToProject(task: Task, project: ProjectTreeElem | undefined) {
    this.tasks.moveTaskToProject(task, project);
  }

  getLabels() {
    return this.labels;
  }

  addNewLabel(request: Label) {
    this.labels.push({
      name: request.name,
      id: request.id,
      color: request.color
    })
  }

  updateLabel(request: Label, labelId: number) {
    let label: LabelDetails | undefined = this.getLabelById(labelId);
    if(label) {
      label.name = request.name;
      label.color = request.color;
    } 
  }

  getLabelById(id: number): LabelDetails | undefined {
    return this.labels.find((a) => a.id == id);
  }
}
