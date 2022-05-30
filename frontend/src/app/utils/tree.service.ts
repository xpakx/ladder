import { Injectable } from '@angular/core';
import { CollabProjectDetails } from 'src/app/project/dto/collab-project-details';
import { CollabTaskDetails } from '../task/dto/collab-task-details';
import { Filter } from 'src/app/filter/dto/filter';
import { FilterDetails } from '../filter/dto/filter-details';
import { Habit } from 'src/app/habit/dto/habit';
import { HabitCompletion } from 'src/app/habit/dto/habit-completion';
import { HabitCompletionDetails } from '../habit/dto/habit-completion-details';
import { HabitDetails } from 'src/app/habit/dto/habit-details';
import { Label } from 'src/app/label/dto/label';
import { LabelDetails } from '../label/dto/label-details';
import { Project } from 'src/app/project/dto/project';
import { ProjectData } from 'src/app/project/dto/project-data';
import { ProjectDetails } from 'src/app/project/dto/project-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { ProjectWithNameAndId } from 'src/app/project/dto/project-with-name-and-id';
import { SyncData } from 'src/app/sync/dto/sync-data';
import { Task } from '../task/dto/task';
import { TaskDetails } from '../task/dto/task-details';
import { TaskTreeElem } from '../task/dto/task-tree-elem';
import { TasksWithProjects } from '../project/dto/tasks-with-projects';
import { UserMin } from '../user/dto/user-min';
import { UserWithData } from 'src/app/sync/dto/user-with-data';
import { CollabProjectTreeService } from 'src/app/project/collab-project-tree.service';
import { CollabTaskTreeService } from 'src/app/task//collab-task-tree.service';
import { FilterTreeService } from 'src/app/filter/filter-tree.service';
import { HabitCompletionTreeService } from 'src/app/habit/habit-completion-tree.service';
import { HabitTreeService } from 'src/app/habit/habit-tree.service';
import { LabelTreeService } from 'src/app/label/label-tree.service';
import { ProjectTreeService } from 'src/app/project/project-tree.service';
import { TaskTreeService } from 'src/app/task/task-tree.service';

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

  load(tree: UserWithData): void {
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

  getProjects(): ProjectTreeElem[] {
    return this.projects.list;
  }

  addNewProject(project: Project, indent: number, parent: ProjectWithNameAndId | null = null): void {
    this.projects.addNewProject(project, indent, parent);
  }

  addNewProjectAfter(project: Project, indent: number, afterId: number): void {
    this.projects.addNewProjectAfter(project, indent, afterId);
  }

  moveProjectAfter(project: Project, indent: number, afterId: number): void {
    this.projects.moveAfter(project, indent, afterId);
  }

  moveProjectAsChild(project: Project, indent: number, parentId: number): void {
    this.projects.moveAsChild(project, indent, parentId);
  }

  moveProjectAsFirst(project: Project): void {
    this.projects.moveAsFirst(project);
  }

  addNewProjectBefore(project: Project, indent: number, beforeId: number): void {
    this.projects.addNewProjectBefore(project, indent, beforeId);
  }
  
  updateProject(project: Project, id: number): void {
    this.projects.updateProject(project, id);
  }

  getProjectById(projectId: number): ProjectTreeElem | undefined {
    return this.projects.getById(projectId);
  }

  getCollabProjectById(projectId: number): ProjectTreeElem | undefined {
    let collab: CollabProjectDetails | undefined = this.collabs.getProjectById(projectId);
    return collab ? {
      id: collab.id,
      name: collab.name,
      parent: null,
      color: collab.color,
      order: 0,
      realOrder: 0,
      hasChildren: false,
      indent: 0,
      parentList: [],
      favorite: collab.favorite,
      collapsed: false,
      modifiedAt: collab.modifiedAt,
      collaborative: true
    } : undefined;
  }

  deleteProject(projectId: number): void {
    let ids = this.projects.deleteProject(projectId);
    for(let id of ids) {
      this.tasks.deleteAllTasksFromProject(id);
    }
  }

  deleteCollabProject(projectId: number): void {
    this.collabs.deleteProject(projectId);
    this.collabTasks.deleteAllTasksFromProject(projectId);
    
  }

  archiveProject(project: Project): void {
    this.projects.archiveProject(project);
    this.tasks.deleteAllTasksFromProject(project.id);
  }

  changeFav(response: Project): void {
    this.projects.changeFav(response);
  }

  filterProjects(text: string): ProjectTreeElem[] {
    return this.projects.filterProjects(text);
  }

  getTasks(): TaskTreeElem[] {
    return this.tasks.list;
  }
  
  getByDate(date: Date): TaskTreeElem[] {
    return this.tasks.getByDate(date);
  }

  getCollabByDate(date: Date): TaskTreeElem[] {
    return this.collabTasks.getByDate(date);
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

  addNewTask(response: Task, projectId: number | undefined, labelIds: number[] = []): void {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.addNewTask(response, project, 0, null, this.getLabelsFromIds(labelIds));
  }

  addNewCollabTask(response: Task, projectId: number | undefined): void {
    let project = projectId ? this.getCollabProjectById(projectId) : undefined;
    this.collabTasks.addNewTask(response, project, 0, null);
  }

  addNewHabit(response: Habit, projectId: number | undefined, labelIds: number[] = []): void {
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

  updateTask(response: Task, projectId: number | undefined, labelIds: number[] = []): void {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.updateTask(response, project, this.getLabelsFromIds(labelIds));
  }

  updateCollabTask(response: Task, projectId: number | undefined, labelIds: number[] = []): void {
    let project = projectId ? this.getCollabProjectById(projectId) : undefined;
    this.collabTasks.updateTask(response, project);
  }

  updateHabit(response: Habit, projectId: number | undefined, labelIds: number[] = []): void {
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.habits.updateHabit(response, project, this.getLabelsFromIds(labelIds));
  }

  changeTaskCompletion(response: Task): void {
    this.tasks.changeTaskCompletion(response);
  }

  changeCollabTaskCompletion(response: Task): void {
    this.collabTasks.changeTaskCompletion(response);
  }

  moveTaskAfter(task: Task, indent: number, afterId: number): void {
    this.tasks.moveAfter(task, indent, afterId);
  }

  moveTaskAsChild(task: Task, indent: number, parentId: number): void {
    this.tasks.moveAsChild(task, indent, parentId);
  }

  moveTaskAsFirst(task: Task, project: ProjectTreeElem | undefined): void {
    this.tasks.moveTaskAsFirst(task, project);
  }

  deleteTask(taskId: number): void {
    this.tasks.deleteTask(taskId);
  }

  deleteCollabTask(taskId: number): void {
    this.collabTasks.deleteTask(taskId);
  }

  deleteHabit(habitId: number): void {
    this.habits.deleteHabit(habitId);
  }

  getTasksByLabel(id: number): TaskTreeElem[] {
    return this.tasks.getTasksByLabel(id);
  }

  updateTaskDate(task: Task): void {
    this.tasks.updateTaskDate(task);
  }

  updateCollabTaskDate(task: Task): void {
    this.collabTasks.updateTaskDate(task);
  }

  addNewTaskAfter(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []): void {
    this.tasks.addNewTaskAfter(task, indent, afterId, project, this.getLabelsFromIds(labelIds));
  }

  addNewTaskBefore(task: Task, indent: number, beforeId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []): void {
    this.tasks.addNewTaskBefore(task, indent, beforeId, project, this.getLabelsFromIds(labelIds));
  }

  addNewTaskAsChild(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []): void {
    this.tasks.addNewTaskAsChild(task, indent, afterId, project, this.getLabelsFromIds(labelIds));
  }

  addNewCollabTaskAfter(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined): void {
    this.collabTasks.addNewTaskAfter(task, indent, afterId, project);
  }

  addNewCollabTaskBefore(task: Task, indent: number, beforeId: number, project: ProjectTreeElem | undefined): void {
    this.collabTasks.addNewTaskBefore(task, indent, beforeId, project);
  }

  addNewCollabTaskAsChild(task: Task, indent: number, afterId: number, project: ProjectTreeElem | undefined): void {
    this.collabTasks.addNewTaskAsChild(task, indent, afterId, project);
  }

  moveTaskToProject(task: Task, project: ProjectTreeElem | undefined): void {
    this.tasks.moveTaskToProject(task, project);
  }

  updateTaskPriority(task: Task): void {
    this.tasks.updateTaskPriority(task);
  }

  updateCollabTaskPriority(task: Task): void {
    this.collabTasks.updateTaskPriority(task);
  }

  updateTaskLabels(task: Task, labels: LabelDetails[]): void {
    this.tasks.updateTaskLabels(task, labels);
  }

  getLabels(): LabelDetails[] {
    return this.labels.labels;
  }

  addNewLabel(request: Label): void {
    this.labels.addNewLabel(request);
  }

  updateLabel(request: Label, labelId: number): void {
    this.labels.updateLabel(request, labelId);
  }

  getLabelById(id: number): LabelDetails | undefined {
    return this.labels.getLabelById(id);
  }

  filterLabels(text: string): LabelDetails[] {
    return this.labels.filterLabels(text);
  }

  deleteLabel(labelId: number): void {
    this.labels.deleteLabel(labelId);
  }

  changeLabelFav(response: Label): void {
    this.labels.changeLabelFav(response);
  }

  addNewLabelBefore(label: Label, beforeId: number): void {
    this.labels.addNewLabelBefore(label, beforeId);
  }

  addNewLabelAfter(label: Label, afterId: number): void {
    this.labels.addNewLabelAfter(label, afterId);
  }

  moveLabelAfter(label: Label, afterId: number): void {
    this.labels.moveAfter(label, afterId);
  }

  moveLabelAsFirst(label: Label): void {
    this.labels.moveAsFirst(label);
  }

  getNumOfUncompletedTasksByLabel(labelId: number): number {
    return this.tasks.getNumOfUncompletedTasksByLabel(labelId);
  }

  duplicateProject(response: TasksWithProjects, mainId: number): void {
    this.projects.addDuplicated(response.projects, mainId);
    this.duplicateTask(response.tasks);
  }

  duplicateTask(response: TaskDetails[], mainId: number | undefined = undefined): void {
    this.tasks.addDuplicated(response, mainId);
  }

  sync(response: SyncData): void {
    this.projects.sync(response.projects);
    this.labels.sync(response.labels);
    this.tasks.sync(response.tasks);
    this.habits.sync(response.habits);
    this.completions.sync(response.habitCompletions);
    this.filters.sync(response.filters);
    this.collabs.sync(response.collabs);
    this.collabTasks.syncTasks(response.collabTasks);
  }

  syncCollabTasks(list: CollabTaskDetails[]): void {
    this.collabTasks.syncTasks(list);
  }

  filterNewCollabsIds(list: CollabProjectDetails[]): number[] {
    let existingIds = this.collabs.list.map((a) => a.project.id);
    return list
      .map((a) => a.id)
      .filter((a) => !existingIds.includes(a));
  }

  syncProject(response: ProjectData): void {
    let projectRestored = this.projects.syncOne(response.project);
    if(projectRestored) {
      this.tasks.sync(response.tasks);
      this.habits.sync(response.habits);
    }
    //this.completions.sync(response.habitCompletions);
  }

  addNewHabitAfter(habit: Habit, afterId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []): void {
    this.habits.addNewHabitAfter(habit, afterId, project, this.getLabelsFromIds(labelIds));
  }

  addNewHabitBefore(habit: Habit, beforeId: number, project: ProjectTreeElem | undefined, labelIds: number[] = []): void {
    this.habits.addNewHabitBefore(habit, beforeId, project, this.getLabelsFromIds(labelIds));
  }

  moveHabitToProject(habit: Habit, project: ProjectTreeElem | undefined): void {
    this.habits.moveHabitToProject(habit, project);
  }

  getHabitById(habitId: number): HabitDetails | undefined {
    return this.habits.getById(habitId);
  }

  getHabits(): HabitDetails[] {
    return this.habits.list;
  }

  updateHabitPriority(habit: Habit): void {
    this.habits.updateHabitPriority(habit);
  }

  completeHabit(habitId: number, completion: HabitCompletion): void {
    this.completions.addCompletion(habitId, completion);
  }

  getCompletions(): HabitCompletionDetails[] {
    return this.completions.list;
  }

  getFilters(): FilterDetails[] {
    return this.filters.list;
  }

  addNewFilter(request: Filter): void {
    this.filters.addNewFilter(request);
  }

  updateFilter(request: Filter, filterId: number): void {
    this.filters.updateFilter(request, filterId);
  }

  addNewFilterBefore(filter: Filter, beforeId: number): void {
    this.filters.addNewFilterBefore(filter, beforeId);
  }

  addNewFilterAfter(filter: Filter, afterId: number): void {
    this.filters.addNewFilterAfter(filter, afterId);
  }

  getFilterById(filterId: number): FilterDetails | undefined {
    return this.filters.getById(filterId);
  }

  deleteFilter(filterId: number): void {
    this.filters.deleteFilter(filterId);
  }

  public getLastProjectArchivization(): Date | undefined {
    return this.projects.getLastArchivization();
  }

  public getLastTaskArchivization(): Date | undefined {
    return this.tasks.getLastArchivization();
  }

  archiveTask(response: Task): void {
    this.tasks.archiveTask(response);
  }

  restoreTask(response: Task, tree: TaskTreeElem[]): void {
    this.tasks.restoreTask(response, tree);
  }

  updateAssignation(response: Task, user: UserMin): void {
    this.tasks.updateAssignation(response, user);
  }

  deleteCompletedTasks(projectId: number): void {
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
