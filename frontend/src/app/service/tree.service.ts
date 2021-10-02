import { Injectable } from '@angular/core';
import { Label } from '../entity/label';
import { LabelDetails } from '../entity/label-details';
import { Project } from '../entity/project';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { ProjectWithNameAndId } from '../entity/project-with-name-and-id';
import { Task } from '../entity/task';
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
    this.sortLabels();
  }

  sortLabels() {
    this.labels.sort((a, b) => a.generalOrder - b.generalOrder);
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
    return this.tasks.getTaskById(taskId);
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

  addNewTaskAfter(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []) {
    this.tasks.addNewTaskAfter(task, indent, afterId, project, this.getLabelsFromIds(labelIds));
  }

  addNewTaskBefore(task: Task, indent: number, beforeId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []) {
    this.tasks.addNewTaskBefore(task, indent, beforeId, project, this.getLabelsFromIds(labelIds));
  }

  moveTaskToProject(task: Task, project: ProjectTreeElem | undefined) {
    this.tasks.moveTaskToProject(task, project);
  }

  updateTaskPriority(task: Task) {
    this.tasks.updateTaskPriority(task);
  }

  getLabels() {
    return this.labels;
  }

  addNewLabel(request: Label) {
    this.labels.push({
      name: request.name,
      id: request.id,
      color: request.color,
      favorite: request.favorite,
      generalOrder: request.generalOrder
    });
    this.sortLabels();
  }

  updateLabel(request: Label, labelId: number) {
    let label: LabelDetails | undefined = this.getLabelById(labelId);
    if(label) {
      label.name = request.name;
      label.color = request.color;
      label.favorite = request.favorite;
    } 
  }

  getLabelById(id: number): LabelDetails | undefined {
    return this.labels.find((a) => a.id == id);
  }

  filterLabels(text: string): LabelDetails[] {
    return this.labels.filter((a) => 
      a.name.toLowerCase().includes(text.toLowerCase())
    );
  }

  deleteLabel(labelId: number) {
    this.labels = this.labels.filter((a) => a.id != labelId);
  }

  changeLabelFav(response: Label) {
    let label = this.getLabelById(response.id);
    if(label) {
      label.favorite = response.favorite;
    }
  }

  addNewLabelBefore(label: Label, beforeId: number) {
    let beforeLabel = this.getLabelById(beforeId);
    if(beforeLabel) {
      let lbl : LabelDetails = beforeLabel;
      label.generalOrder = lbl.generalOrder;
      let labels = this.labels
        .filter((a) => a.generalOrder >= lbl.generalOrder);
        for(let lab of labels) {
          lab.generalOrder = lab.generalOrder + 1;
        }
      this.addNewLabel(label);
    }
  }

  addNewLabelAfter(label: Label, afterId: number) {
    let afterLabel = this.getLabelById(afterId);
    if(afterLabel) {
      let lbl : LabelDetails = afterLabel;
      label.generalOrder = lbl.generalOrder + 1;
      let labels = this.labels
        .filter((a) => a.generalOrder > lbl.generalOrder);
        for(let lab of labels) {
          lab.generalOrder = lab.generalOrder + 1;
        }
      this.addNewLabel(label);
    }
  }

  moveLabelAfter(label: Label, afterId: number) {
    let afterLabel = this.getLabelById(afterId);
    let movedLabel = this.getLabelById(label.id);
    if(afterLabel && movedLabel) {
      let lbl : LabelDetails = afterLabel;
      let labels = this.labels
        .filter((a) => a.generalOrder > lbl.generalOrder);
        for(let lab of labels) {
          lab.generalOrder = lab.generalOrder + 1;
        }
      
      movedLabel.generalOrder = afterLabel.generalOrder+1;

      this.sortLabels();
    }
  }

  moveLabelAsFirst(label: Label) {
    let movedLabel = this.getLabelById(label.id);
    if(movedLabel) {
      for(let lbl of this.labels) {
        lbl.generalOrder = lbl.generalOrder + 1;
      }
      movedLabel.generalOrder = 1;
      this.sortLabels();
    }
  }
}
