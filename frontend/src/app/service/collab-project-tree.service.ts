import { Injectable } from '@angular/core';
import { CollabProjectData } from '../entity/collab-project-data';
import { CollabProjectDetails } from '../entity/collab-project-details';
import { Project } from '../entity/project';

@Injectable({
  providedIn: 'root'
})
export class CollabProjectTreeService  {
  public list: CollabProjectDetails[] = [];
  public collapsed: boolean = true;

  constructor() {  }

  load(projects: CollabProjectData[] = []) {
    this.list = projects.map((a) => a.project);
    this.sort();
  }

  sort() {
    this.list.sort((a, b) => a.id - b.id);
  }

  addNewProject(project: Project, indent: number) {
    this.list.push({
      id: project.id,
      name: project.name,
      color: project.color,
      favorite: project.favorite,
      generalOrder: project.order,
      modifiedAt: new Date(project.modifiedAt)
    });
    this.sort();
  }

  deleteProject(projectId: number) {
    this.list = this.list.filter((a) => a.id != projectId);
  }

  getProjects(): CollabProjectDetails[] {
    return this.list;
  }

  isEmpty(): boolean {
    return this.list.length == 0;
  }

  getProjectById(id: number): CollabProjectDetails | undefined {
    return this.list.find((a) => a.id == id);
  }

  sync(projects: CollabProjectDetails[]) {
    for(let project of projects) {
      let projectWithId = this.getProjectById(project.id);
      if(projectWithId) {
        projectWithId.color = project.color;
        projectWithId.favorite = project.favorite;
        projectWithId.generalOrder = project.generalOrder;
        projectWithId.name = project.name;
        projectWithId.modifiedAt = new Date(project.modifiedAt);
      } else {
        this.list.push(project);
      }
    }
    this.sort();
  }
}
