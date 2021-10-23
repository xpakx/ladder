import { Injectable } from '@angular/core';
import { Label } from '../entity/label';
import { LabelDetails } from '../entity/label-details';
import { Project } from '../entity/project';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { ProjectWithNameAndId } from '../entity/project-with-name-and-id';
import { SyncData } from '../entity/sync-data';
import { Task } from '../entity/task';
import { TaskDetails } from '../entity/task-details';
import { TaskTreeElem } from '../entity/task-tree-elem';
import { TasksWithProjects } from '../entity/tasks-with-projects';
import { UserWithData } from '../entity/user-with-data';
import { LabelTreeService } from './label-tree.service';
import { ProjectTreeService } from './project-tree.service';
import { TaskTreeService } from './task-tree.service';

@Injectable({
  providedIn: 'root'
})
export class TreeService {
  public loaded: boolean = false;
  public projectCollapsed: boolean = true;
  public labelCollapsed: boolean = true;
  
  constructor(private projects: ProjectTreeService, private tasks: TaskTreeService,
    private labels: LabelTreeService) { }

  isLoaded(): boolean {
    return this.loaded;
  }

  load(tree: UserWithData) {
    this.loaded = true;
    this.projectCollapsed = tree.projectCollapsed;
    this.projects.load(tree.projects);
    this.tasks.load(tree.tasks);
    this.labels.load(tree.labels);
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
    this.projects.moveAfter(project, indent, afterId);
  }

  moveProjectAsChild(project: Project, indent: number, parentId: number) {
    this.projects.moveAsChild(project, indent, parentId);
  }

  moveProjectAsFirst(project: Project) {
    this.projects.moveAsFirst(project);
  }

  addNewProjectBefore(project: Project, indent: number, beforeId: number) {
    this.projects.addNewProjectBefore(project, indent, beforeId);
  }
  
  updateProject(project: Project, id: number) {
    this.projects.updateProject(project, id);
  }

  getProjectById(projectId: number): ProjectTreeElem | undefined {
    return this.projects.getById(projectId);
  }

  deleteProject(projectId: number) {
    let ids = this.projects.deleteProject(projectId);
    for(let id of ids) {
      this.tasks.deleteAllTasksFromProject(id);
    }
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

  getNumOfCompletedTasksByParent(parentId: number): number {
    return this.tasks.getNumOfCompletedTasksByParent(parentId);
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
    return this.tasks.getById(taskId);
  }

  getTasksByProject(projectId: number): TaskTreeElem[] {
    return this.tasks.getTasksByProject(projectId);
  }

  addNewTask(response: Task, projectId: number | undefined, labelIds: number[] = []) {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.addNewTask(response, project, 0, null, this.getLabelsFromIds(labelIds));
  }

  private getLabelsFromIds(labelIds: number[]): LabelDetails[] {
    let labels: LabelDetails[] = [];
    for (let id of labelIds) {
      let label = this.getLabelById(id);
      if (label) { labels.push(label); }
    }
    return labels;
  }

  updateTask(response: Task, projectId: number | undefined, labelIds: number[] = []) {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.updateTask(response, project, this.getLabelsFromIds(labelIds));
  }

  changeTaskCompletion(response: Task) {
    this.tasks.changeTaskCompletion(response);
  }

  moveTaskAfter(task: Task, indent: number, afterId: number) {
    this.tasks.moveAfter(task, indent, afterId);
  }

  moveTaskAsChild(task: Task, indent: number, parentId: number) {
    this.tasks.moveAsChild(task, indent, parentId);
  }

  moveTaskAsFirst(task: Task, project: ProjectTreeElem | undefined) {
    this.tasks.moveTaskAsFirst(task, project);
  }

  deleteTask(taskId: number) {
    this.tasks.deleteTask(taskId);
  }

  getTasksByLabel(id: number): TaskTreeElem[] {
    return this.tasks.getTasksByLabel(id);
  }

  updateTaskDate(task: Task) {
    this.tasks.updateTaskDate(task);
  }

  addNewTaskAfter(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []) {
    this.tasks.addNewTaskAfter(task, indent, afterId, project, this.getLabelsFromIds(labelIds));
  }

  addNewTaskBefore(task: Task, indent: number, beforeId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []) {
    this.tasks.addNewTaskBefore(task, indent, beforeId, project, this.getLabelsFromIds(labelIds));
  }

  addNewTaskAsChild(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []) {
    this.tasks.addNewTaskAsChild(task, indent, afterId, project, this.getLabelsFromIds(labelIds));
  }

  moveTaskToProject(task: Task, project: ProjectTreeElem | undefined) {
    this.tasks.moveTaskToProject(task, project);
  }

  updateTaskPriority(task: Task) {
    this.tasks.updateTaskPriority(task);
  }

  updateTaskLabels(task: Task, labels: LabelDetails[]) {
    this.tasks.updateTaskLabels(task, labels);
  }

  getLabels() {
    return this.labels.labels;
  }

  addNewLabel(request: Label) {
    this.labels.addNewLabel(request);
  }

  updateLabel(request: Label, labelId: number) {
    this.labels.updateLabel(request, labelId);
  }

  getLabelById(id: number): LabelDetails | undefined {
    return this.labels.getLabelById(id);
  }

  filterLabels(text: string): LabelDetails[] {
    return this.labels.filterLabels(text);
  }

  deleteLabel(labelId: number) {
    this.labels.deleteLabel(labelId);
  }

  changeLabelFav(response: Label) {
    this.labels.changeLabelFav(response);
  }

  addNewLabelBefore(label: Label, beforeId: number) {
    this.labels.addNewLabelBefore(label, beforeId);
  }

  addNewLabelAfter(label: Label, afterId: number) {
    this.labels.addNewLabelAfter(label, afterId);
  }

  moveLabelAfter(label: Label, afterId: number) {
    this.labels.moveAfter(label, afterId);
  }

  moveLabelAsFirst(label: Label) {
    this.labels.moveAsFirst(label);
  }

  getNumOfUncompletedTasksByLabel(labelId: number): number {
    return this.tasks.getNumOfUncompletedTasksByLabel(labelId);
  }

  duplicateProject(response: TasksWithProjects) {
    this.projects.addDuplicated(response.projects);
    this.duplicateTask(response.tasks);
  }

  duplicateTask(response: TaskDetails[]) {
    this.tasks.addDuplicated(response);
  }

  sync(response: SyncData) {
    this.projects.sync(response.projects);
    this.labels.sync(response.labels);
  }
}
