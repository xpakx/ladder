import { Injectable } from '@angular/core';
import { CollabProjectData } from 'src/app/project/dto/collab-project-data';
import { CollabProjectDetails } from 'src/app/project/dto/collab-project-details';

@Injectable({
  providedIn: 'root'
})
export class CollabProjectTreeService  {
  public list: CollabProjectData[] = [];
  public collapsed: boolean = true;

  constructor() {  }

  load(projects: CollabProjectData[] = []) {
    this.list = projects;
    this.sort();
  }

  sort() {
    this.list.sort((a, b) => a.id - b.id);
  }

  deleteProject(projectId: number) {
    this.list = this.list.filter((a) => a.project.id != projectId);
  }

  getProjects(): CollabProjectDetails[] {
    return this.list.map((a) => a.project);
  }

  isEmpty(): boolean {
    return this.list.length == 0;
  }

  getProjectById(id: number): CollabProjectDetails | undefined {
    return this.list.map((a) => a.project).find((a) => a.id == id);
  }

  getCollabByProjectId(id: number): CollabProjectData | undefined {
    return this.list.find((a) => a.project.id == id);
  }

  sync(projects: CollabProjectData[]) {
    for(let project of projects) {
      let projectWithId = this.getCollabByProjectId(project.project.id);
      if(projectWithId) {
        projectWithId.project.color = project.project.color;
        projectWithId.project.favorite = project.project.favorite;
        projectWithId.project.generalOrder = project.project.generalOrder;
        projectWithId.project.name = project.project.name;
        projectWithId.project.modifiedAt = new Date(project.project.modifiedAt);
        projectWithId.editionAllowed = project.editionAllowed;
        projectWithId.taskCompletionAllowed = project.taskCompletionAllowed;
        projectWithId.modifiedAt = new Date(project.modifiedAt);
      } else {
        this.list.push(project);
      }
    }
    this.sort();
  }
}
