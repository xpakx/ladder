import { Injectable } from '@angular/core';
import { LabelDetails } from '../entity/label-details';
import { Project } from '../entity/project';
import { ProjectDetails } from '../entity/project-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { Task } from '../entity/task';
import { TaskDetails } from '../entity/task-details';
import { UserWithData } from '../entity/user-with-data';

@Injectable({
  providedIn: 'root'
})
export class TreeService {
  public projects: ProjectTreeElem[] = [];
  public tasks: TaskDetails[] = [];
  public labels: LabelDetails[] = [];
  public loaded: boolean = false;

  constructor() { }

  isLoaded(): boolean {
    return this.loaded;
  }

  load(tree: UserWithData) {
    this.loaded = true;
    this.projects = this.transformAll(tree.projects);
    this.projects.sort((a, b) => a.order - b.order);
    this.calculateRealOrder();
    this.projects.sort((a, b) => a.realOrder - b.realOrder);
    this.tasks = tree.tasks;
    this.labels = tree.labels;
  }

  addNewProject(project: Project, indent: number) {
    this.projects.push({
      id: project.id,
      name: project.name,
      parent: null,
      color: project.color,
      order: project.order,
      realOrder: this.projects.length+1,
      hasChildren: false,
      indent: indent,
      parentList: [],
      favorite: project.favorite
    });
    this.projects.sort((a, b) => a.order - b.order);
    this.calculateRealOrder();
    this.projects.sort((a, b) => a.realOrder - b.realOrder);
  }

  updateProject(project: Project, id: number) {
    let projectElem = this.getProjectById(id)
    if(projectElem) {
      projectElem.name = project.name;
      projectElem.color = project.color;
      projectElem.favorite = project.favorite;

    }
  }

  transformAll(projects: ProjectDetails[]):  ProjectTreeElem[] {
    return projects.map((a) => this.transform(a, projects));
  }

  transform(project: ProjectDetails, projects: ProjectDetails[]): ProjectTreeElem {
    let indent: number = this.getProjectIndent(project.id, projects);
    return {
      id: project.id,
      name: project.name,
      parent: project.parent,
      color: project.color,
      order: project.generalOrder,
      realOrder: project.generalOrder,
      hasChildren: this.hasChildrenByProjectId(project.id, projects),
      indent: indent,
      parentList: [],
      favorite: project.favorite
    }
  }

  calculateRealOrder() {
    let proj = this.projects.filter((a) => a.indent == 0);
    var offset = 0;
    for(let project of proj) {
      offset += this.countAllChildren(project, offset) +1;
    }
  }

  countAllChildren(project: ProjectTreeElem, offset: number, parent?: ProjectTreeElem): number {
    project.realOrder = offset;
    offset += 1;
    
    if(parent) {
		project.parentList = [...parent.parentList];
		project.parentList.push(parent.id);
	}

    if(!project.hasChildren) {
      return 0;
    }

    let children = this.projects.filter((a) => a.parent?.id == project.id);
    var num = 0;
    for(let proj of children) {
      let childNum = this.countAllChildren(proj, offset, project);
      num += childNum+1;
      offset += childNum+1;      
    } 
    return num;
  }

  hasChildrenByProjectId(projectId: number, projects: ProjectDetails[]): boolean {
    return projects.find((a) => a.parent?.id == projectId) != null;
  }

  getProjectIndent(projectId: number, projects: ProjectDetails[]): number {
    let parentId: number | undefined = projects.find((a) => a.id == projectId)?.parent?.id;
    let counter = 0;
    while(parentId != null) {
      counter +=1;
      parentId = projects.find((a) => a.id == parentId)?.parent?.id;
    }
    return counter;
  }
  
  getByDate(date: Date): TaskDetails[] {
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

  getProjectById(projectId: number): ProjectTreeElem | undefined {
    return this.projects.find((a) => a.id == projectId);
  }

  getTasksByProject(projectId: number): TaskDetails[] {
    return this.tasks.filter((a) => 
      a.project && a.project.id == projectId
    );
  }

  deleteProject(projectId: number) {
    this.projects = this.projects.filter((a) => a.id != projectId);
  }


  changeFav(response: Project) {
    let project = this.getProjectById(response.id);
    if(project) {
      project.favorite = response.favorite;
    }
  }
 

  addNewTask(response: Task, projectId: number | undefined) {
    this.tasks.push({
      id:response.id,
      title: response.title,
      description: response.description,
      project: null,
      parent: null,
      due: null,
      completed: false
    })
  }

  filterProjects(text: string): ProjectTreeElem[] {
    return this.projects.filter((a) => 
      a.name.toLowerCase().includes(text.toLowerCase())
    );
  }
}
