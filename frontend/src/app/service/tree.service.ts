import { transformAll } from '@angular/compiler/src/render3/r3_ast';
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
      }
    ];
    this.projects = this.transformAll(testProjects);
    this.projects.sort((a, b) => a.realOrder - b.realOrder);

    this.tasks.push(
      {id: 0,
        title: "Task 1",
        description: "",
        project: {id: 0, name: "Project 1"},
        parent: null,
        due: new Date(),
        completed: false
    });
  }

  load(tree: UserWithData) {
    this.projects = this.transformAll(tree.projects);
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
      realOrder: this.getProjectIndent(project.id, projects) * projects.length + project.order,
      hasChildren: this.hasChildrenByProjectId(project.id, projects),
      indent: indent
    }
  }

  hasChildrenByProjectId(projectId: number, projects: ProjectDetails[]): boolean {
    return projects.find((a) => a.parent?.id == projectId) != null;
  }

  getProjectIndent(projectId: number, projects: ProjectDetails[]): number {
    let parentId: number | undefined = projects.find((a) => a.id == projectId)?.parent?.id;
    let counter = 0;
    while(parentId) {
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
