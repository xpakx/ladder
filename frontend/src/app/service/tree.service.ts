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
  public projectCollapsed: boolean = true;

  constructor() { }

  isLoaded(): boolean {
    return this.loaded;
  }

  load(tree: UserWithData) {
    this.loaded = true;
    this.projectCollapsed = tree.projectCollapsed;
    this.projects = this.transformAll(tree.projects);
    this.projects.sort((a, b) => a.order - b.order);
    this.calculateRealOrder();
    this.projects.sort((a, b) => a.realOrder - b.realOrder);
    this.tasks = tree.tasks;
    this.tasks.forEach((a) => a.due = a.due ? new Date(a.due) : null);
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
      favorite: project.favorite,
      collapsed: true
    });
    this.projects.sort((a, b) => a.order - b.order);
    this.calculateRealOrder();
    this.projects.sort((a, b) => a.realOrder - b.realOrder);
  }

  addNewProjectAfter(project: Project, indent: number, afterId: number) {
    let afterProject = this.getProjectById(afterId);
    if(afterProject) {
      let proj : ProjectTreeElem = afterProject;
      project.order = proj.order+1;
      let projects = this.projects
        .filter((a) => a.parent == proj.parent)
        .filter((a) => a.order > proj.order);
        for(let pro of projects) {
          pro.order = pro.order + 1;
        }
        this.addNewProject(project, indent);
    }
  }


  moveProjectAfter(project: Project, indent: number, afterId: number) {
    let afterProject = this.getProjectById(afterId);
    let movedProject = this.getProjectById(project.id);
    if(afterProject && movedProject) {
      let proj : ProjectTreeElem = afterProject;
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getProjectById(movedProject.parent.id) : undefined;
      let projects = this.projects
        .filter((a) => a.parent == proj.parent)
        .filter((a) => a.order > proj.order);
        for(let pro of projects) {
          pro.order = pro.order + 1;
        }
      
      movedProject.indent = indent;
      movedProject.parent = afterProject.parent;
      movedProject.order = afterProject.order+1;

      this.recalculateChildrenIndent(movedProject.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(proj);

      this.projects.sort((a, b) => a.order - b.order);
      this.calculateRealOrder();
      this.projects.sort((a, b) => a.realOrder - b.realOrder);
    }
  }

  recalculateChildrenIndent(projectId: number, indent: number) {
    let children = this.projects
    .filter((a) => a.parent && a.parent.id == projectId);
    for(let child of children) {
      child.indent = indent;
      this.recalculateChildrenIndent(child.id, indent+1);
    }
  }

  recalculateHasChildren(project: ProjectTreeElem) {
    let children = this.projects.filter((a) => a.parent && a.parent.id == project.id);
    project.hasChildren = children.length > 0 ? true : false;
    for(let parent of project.parentList) {
      let parentChildren = this.projects.filter((a) => a.parent && a.parent.id == parent.id);
      parent.hasChildren = parentChildren.length > 0 ? true : false;
    }
  }

  moveProjectAsChild(project: Project, indent: number, parentId: number) {
    let parentProject = this.getProjectById(parentId);
    let movedProject = this.getProjectById(project.id);
    if(parentProject && movedProject) {
      let proj : ProjectTreeElem = parentProject;
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getProjectById(movedProject.parent.id) : undefined;
      let projects = this.projects
        .filter((a) => a.parent == proj);
        for(let pro of projects) {
          pro.order = pro.order + 1;
        }
      
      movedProject.indent = indent;
      movedProject.order = 1;
      movedProject.parent = parentProject;

      console.log("My id: " + movedProject.id + ", my parent id: " + (movedProject.parent ? movedProject.parent.id : "null"))

      this.recalculateChildrenIndent(movedProject.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(proj);

      this.projects.sort((a, b) => a.order - b.order);
      this.calculateRealOrder();
      this.projects.sort((a, b) => a.realOrder - b.realOrder);
    }
  }

  addNewProjectBefore(project: Project, indent: number, beforeId: number) {
    let beforeProject = this.getProjectById(beforeId);
    if(beforeProject) {
      let proj : ProjectTreeElem = beforeProject;
      project.order = proj.order;
      let projects = this.projects
        .filter((a) => a.parent == proj.parent)
        .filter((a) => a.order >= proj.order);
        for(let pro of projects) {
          pro.order = pro.order + 1;
        }
        this.addNewProject(project, indent);
    }
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
      favorite: project.favorite,
      collapsed: project.collapsed
    }
  }

  calculateRealOrder() {
    let proj = this.projects.filter((a) => a.indent == 0);
    var offset = 0;
    for(let project of proj) {
      project.parentList = [];
      offset += this.countAllChildren(project, offset) +1;
    }
  }

  countAllChildren(project: ProjectTreeElem, offset: number, parent?: ProjectTreeElem): number {
    project.realOrder = offset;
    offset += 1;
    
    if(parent) {
      project.parentList = [...parent.parentList];
      project.parentList.push(parent);
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
    return 0//this.getByDate(new Date()).length;
  }

  getProjectById(projectId: number): ProjectTreeElem | undefined {
    return this.projects.find((a) => a.id == projectId);
  }

  getTaskById(taskId: number): TaskDetails | undefined {
    return this.tasks.find((a) => a.id == taskId);
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
    let project = projectId ? this.getProjectById(projectId) : undefined;
    this.tasks.push({
      id:response.id,
      title: response.title,
      description: response.description,
      project: project ? project : null,
      parent: null,
      due: response.due ? new Date(response.due) : null,
      completed: false,
      projectOrder: response.projectOrder
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

  filterProjects(text: string): ProjectTreeElem[] {
    return this.projects.filter((a) => 
      a.name.toLowerCase().includes(text.toLowerCase())
    );
  }

  changeTaskCompletion(response: Task) {
    let task = this.getTaskById(response.id);
    if(task) {
      task.completed = response.completed;
    }
  }
}
