import { Injectable } from '@angular/core';
import { LabelDetails } from '../entity/label-details';
import { ProjectDetails } from '../entity/project-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { TaskDetails } from '../entity/task-details';
import { UserWithData } from '../entity/user-with-data';

@Injectable({
  providedIn: 'root'
})
export class TreeService {
  public projects: ProjectTreeElem[] = [];
  public tasks: TaskDetails[] = [];
  public labels: LabelDetails[] = [];

  constructor() { 
    let testProjects = [
      {
        id: 0,
        name: "Project 1", 
        parent: null,
        color: "#DB4035",
        order: 0,
        realOrder: undefined
      },
      {
        id: 1,
        name: "Project 2", 
        parent: null,
        color: "#35D0DB",
        order: 0,
        realOrder: undefined
      },
      {
        id: 2,
        name: "Project 3", 
        parent: {id: 1, name: "Project 2"},
        color: "#DB9335",
        order: 0,
        realOrder: undefined
      },
      {
        id: 3,
        name: "Project 4", 
        parent: {id: 0, name: "Project 1"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 4,
        name: "Project 5", 
        parent: {id: 2, name: "Project 3"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 5,
        name: "Project 6", 
        parent: {id: 0, name: "Project 1"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      }
    ];
    this.projects = this.transformAll(testProjects);
    this.projects.sort((a, b) => a.order - b.order);
    this.calculateRealOrder();
    this.projects.sort((a, b) => a.realOrder - b.realOrder);

    this.tasks = [
      {
        id: 0,
        title: "Task 1",
        description: "",
        project: {id: 0, name: "Project 1"},
        parent: null,
        due: new Date(),
        completed: false
      }
    ];
  }

  load(tree: UserWithData) {
    this.projects = this.transformAll(tree.projects);
    this.projects.sort((a, b) => a.order - b.order);
    this.calculateRealOrder();
    this.projects.sort((a, b) => a.realOrder - b.realOrder);
    this.tasks = tree.tasks;
    this.labels = tree.labels;
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
      order: project.order,
      realOrder: project.order,
      hasChildren: this.hasChildrenByProjectId(project.id, projects),
      indent: indent
    }
  }

  calculateRealOrder() {
    let proj = this.projects.filter((a) => a.indent == 0);
    var offset = 0;
    for(let project of proj) {
      offset += this.countAllChildren(project, offset) +1;
    }
  }

  countAllChildren(project: ProjectTreeElem, offset: number): number {
    project.realOrder = offset;
    offset += 1;

    if(!project.hasChildren) {
      return 0;
    }

    let children = this.projects.filter((a) => a.parent?.id == project.id);
    var num = 0;
    for(let proj of children) {
      let childNum = this.countAllChildren(proj, offset);
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
    return this.tasks.filter((a) => {
      a.due.getDate() === date.getDate() && a.due.getMonth() === date.getMonth() && a.due.getFullYear() === date.getFullYear() 
    });
  }

  getNumOfUncompletedTasksByProject(projectId: number): number {
    return this.tasks.filter((a) => {
      a.project && a.project.id == projectId && !a.completed
    }).length;
  }
}
