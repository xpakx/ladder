import { Injectable } from '@angular/core';
import { Filter } from '../entity/filter';
import { FilterDetails } from '../entity/filter-details';
import { Habit } from '../entity/habit';
import { HabitCompletion } from '../entity/habit-completion';
import { HabitDetails } from '../entity/habit-details';
import { Label } from '../entity/label';
import { LabelDetails } from '../entity/label-details';
import { Project } from '../entity/project';
import { ProjectData } from '../entity/project-data';
import { ProjectDetails } from '../entity/project-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { ProjectWithNameAndId } from '../entity/project-with-name-and-id';
import { SyncData } from '../entity/sync-data';
import { Task } from '../entity/task';
import { TaskDetails } from '../entity/task-details';
import { TaskTreeElem } from '../entity/task-tree-elem';
import { TasksWithProjects } from '../entity/tasks-with-projects';
import { UserWithData } from '../entity/user-with-data';
import { CollabProjectTreeService } from './collab-project-tree.service';
import { CollabTaskTreeService } from './collab-task-tree.service';
import { FilterTreeService } from './filter-tree.service';
import { HabitCompletionTreeService } from './habit-completion-tree.service';
import { HabitTreeService } from './habit-tree.service';
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
  public filterCollapsed: boolean = true;
  
  constructor(private projects: ProjectTreeService, private tasks: TaskTreeService,
    private labels: LabelTreeService, private habits: HabitTreeService, 
    private completions: HabitCompletionTreeService, private filters: FilterTreeService,
    private collabs: CollabProjectTreeService, private collabTasks: CollabTaskTreeService) { }

  isLoaded(): boolean {
    return this.loaded;
  }

  load(tree: UserWithData) {
    this.loaded = true;
    this.projectCollapsed = tree.projectCollapsed;
    this.projects.load(tree.projects);
    this.tasks.load(tree.tasks);
    this.labels.load(tree.labels);
    this.habits.load(tree.habits);
    this.completions.load(tree.todayHabitCompletions);
    this.filters.load(tree.filters);
    this.collabs.load(tree.collabs);
    this.collabTasks.loadTasks(tree.collabTasks)
  }

  transformTasks(tasks: TaskDetails[]): TaskTreeElem[] {
    return this.tasks.transformAndReturn(tasks);
  }

  transformProject(project: ProjectDetails): ProjectTreeElem {
    return this.projects.transformAndReturn(project);
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

  archiveProject(project: Project) {
    this.projects.archiveProject(project);
    this.tasks.deleteAllTasksFromProject(project.id);
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

  getByDateBetween(date1: Date, date2: Date): TaskTreeElem[] {
    return this.tasks.getByDateBetween(date1, date2);
  }

  getByDateOverdue(date: Date): TaskTreeElem[] {
    return this.tasks.getOverdue(date);
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

  getNumOfCompletedCollabTasksByParent(parentId: number): number {
    return this.collabTasks.getNumOfCompletedTasksByParent(parentId);
  }

  getNumOfCollabTasksByParent(parentId: number): number {
    return this.collabTasks.getNumOfTasksByParent(parentId);
  }

  getNumOfUncompletedTasksInInbox(): number {
    return this.tasks.getNumOfUncompletedTasksInInbox();
  }

  getNumOfUncompletedTasksToday(): number {
    return this.tasks.getNumOfUncompletedTasksToday();
  }

  getNumOfUncompletedTasksByCollabProject(projectId: number): number {
    return this.collabTasks.getNumOfUncompletedTasksByProject(projectId);
  }

  getTaskById(taskId: number): TaskTreeElem | undefined {
    return this.tasks.getById(taskId);
  }

  getCollabTaskById(taskId: number): TaskTreeElem | undefined {
    return this.collabTasks.getById(taskId);
  }

  getTasksByProject(projectId: number): TaskTreeElem[] {
    return this.tasks.getTasksByProject(projectId);
  }

  addNewTask(response: Task, projectId: number | undefined, labelIds: number[] = []) {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.addNewTask(response, project, 0, null, this.getLabelsFromIds(labelIds));
  }

  addNewHabit(response: Habit, projectId: number | undefined, labelIds: number[] = []) {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.habits.addNewHabit(response, project, this.getLabelsFromIds(labelIds));
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

  updateHabit(response: Habit, projectId: number | undefined, labelIds: number[] = []) {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.habits.updateHabit(response, project, this.getLabelsFromIds(labelIds));
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

  deleteCollabTask(taskId: number) {
    this.collabTasks.deleteTask(taskId);
  }

  deleteHabit(habitId: number) {
    this.habits.deleteHabit(habitId);
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
    this.tasks.sync(response.tasks);
    this.habits.sync(response.habits);
    this.completions.sync(response.habitCompletions);
    this.filters.sync(response.filters);
  }

  syncProject(response: ProjectData) {
    let projectRestored = this.projects.syncOne(response.project);
    if(projectRestored) {
      this.tasks.sync(response.tasks);
      this.habits.sync(response.habits);
    }
    //this.completions.sync(response.habitCompletions);
  }

  addNewHabitAfter(habit: Habit, afterId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []) {
    this.habits.addNewHabitAfter(habit, afterId, project, this.getLabelsFromIds(labelIds));
  }

  addNewHabitBefore(habit: Habit, beforeId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []) {
    this.habits.addNewHabitBefore(habit, beforeId, project, this.getLabelsFromIds(labelIds));
  }

  moveHabitToProject(habit: Habit, project: ProjectTreeElem | undefined) {
    this.habits.moveHabitToProject(habit, project);
  }

  getHabitById(habitId: number): HabitDetails | undefined {
    return this.habits.getById(habitId);
  }

  getHabits() {
    return this.habits.list;
  }

  updateHabitPriority(habit: Habit) {
    this.habits.updateHabitPriority(habit);
  }

  completeHabit(habitId: number, completion: HabitCompletion) {
    this.completions.addCompletion(habitId, completion);
  }

  getCompletions() {
    return this.completions.list;
  }

  getFilters() {
    return this.filters.list;
  }

  addNewFilter(request: Filter) {
    this.filters.addNewFilter(request);
  }

  updateFilter(request: Filter, filterId: number) {
    this.filters.updateFilter(request, filterId);
  }

  addNewFilterBefore(filter: Filter, beforeId: number) {
    this.filters.addNewFilterBefore(filter, beforeId);
  }

  addNewFilterAfter(filter: Filter, afterId: number) {
    this.filters.addNewFilterAfter(filter, afterId);
  }

  getFilterById(filterId: number): FilterDetails | undefined {
    return this.filters.getById(filterId);
  }

  deleteFilter(filterId: number) {
    this.filters.deleteFilter(filterId);
  }

  public getLastProjectArchivization(): Date | undefined {
    return this.projects.getLastArchivization();
  }

  public getLastTaskArchivization(): Date | undefined {
    return this.tasks.getLastArchivization();
  }

  archiveTask(response: Task) {
    this.tasks.archiveTask(response);
  }

  restoreTask(response: Task, tree: TaskTreeElem[]) {
    this.tasks.restoreTask(response, tree);
  }

  deleteCompletedTasks(projectId: number) {
    let tasks = this.tasks.getTasksByProject(projectId)
      .filter((a) => a.completed);
    for(let task of tasks) {
      this.tasks.deleteTask(task.id);
    }
  }

  hasCollabs(): boolean {
    return !this.collabs.isEmpty();
  }
}
