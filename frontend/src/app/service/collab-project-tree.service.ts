import { Injectable } from '@angular/core';
import { CollabProjectDetails } from '../entity/collab-project-details';
import { Project } from '../entity/project';

@Injectable({
  providedIn: 'root'
})
export class CollabProjectTreeService  {
  public list: CollabProjectDetails[] = [];
  public collapsed: boolean = true;

  constructor() {  }

  load(projects: CollabProjectDetails[] = []) {
    this.list = projects;
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
    console.log(JSON.stringify(this.list))
    return this.list;
  }

  isEmpty(): boolean {
    return this.list.length == 0;
  }
}
