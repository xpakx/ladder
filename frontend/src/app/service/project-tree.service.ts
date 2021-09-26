import { Injectable } from '@angular/core';
import { Project } from '../entity/project';
import { ProjectDetails } from '../entity/project-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { ProjectWithNameAndId } from '../entity/project-with-name-and-id';
import { IndentableService } from './indentable-service';

@Injectable({
  providedIn: 'root'
})
export class ProjectTreeService extends IndentableService<ProjectWithNameAndId> {
  public list: ProjectTreeElem[] = [];

  constructor() { super() }

  load(projects: ProjectDetails[]) {
    this.list = this.transformAll(projects);
    this.sort();
  }

  private transformAll(projects: ProjectDetails[]):  ProjectTreeElem[] {
    return projects.map((a) => this.transform(a, projects));
  }

  private transform(project: ProjectDetails, projects: ProjectDetails[]): ProjectTreeElem {
    let indent: number = this.getIndent(project.id, projects);
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

  private hasChildrenByProjectId(projectId: number, projects: ProjectDetails[]): boolean {
    return projects.find((a) => a.parent?.id == projectId) != null;
  }

  private getIndent(projectId: number, projects: ProjectDetails[]): number {
    let parentId: number | undefined = projects.find((a) => a.id == projectId)?.parent?.id;
    let counter = 0;
    while(parentId != null) {
      counter +=1;
      parentId = projects.find((a) => a.id == parentId)?.parent?.id;
    }
    return counter;
  }

  addNewProject(project: Project, indent: number, parent: ProjectWithNameAndId | null = null) {
    this.list.push({
      id: project.id,
      name: project.name,
      parent: parent,
      color: project.color,
      order: project.order,
      realOrder: this.list.length+1,
      hasChildren: false,
      indent: indent,
      parentList: [],
      favorite: project.favorite,
      collapsed: true
    });
    this.sort();
  }

  addNewProjectAfter(project: Project, indent: number, afterId: number) {
    let afterProject = this.getProjectById(afterId);
    if(afterProject) {
      let proj : ProjectTreeElem = afterProject;
      project.order = proj.order+1;
      let projects = this.list
        .filter((a) => a.parent == proj.parent)
        .filter((a) => a.order > proj.order);
        for(let pro of projects) {
          pro.order = pro.order + 1;
        }
        this.addNewProject(project, indent, proj.parent);
    }
  }

  moveProjectAfter(project: Project, indent: number, afterId: number) {
    let afterProject = this.getProjectById(afterId);
    let movedProject = this.getProjectById(project.id);
    if(afterProject && movedProject) {
      let proj : ProjectTreeElem = afterProject;
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getProjectById(movedProject.parent.id) : undefined;
      let projects = this.list
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

      this.sort();
    }
  }

  moveProjectAsChild(project: Project, indent: number, parentId: number) {
    let parentProject = this.getProjectById(parentId);
    let movedProject = this.getProjectById(project.id);
    if(parentProject && movedProject) {
      let proj : ProjectTreeElem = parentProject;
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getProjectById(movedProject.parent.id) : undefined;
      let projects = this.list
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

      this.sort();
    }
  }

  addNewProjectBefore(project: Project, indent: number, beforeId: number) {
    let beforeProject = this.getProjectById(beforeId);
    if(beforeProject) {
      let proj : ProjectTreeElem = beforeProject;
      project.order = proj.order;
      let projects = this.list
        .filter((a) => a.parent == proj.parent)
        .filter((a) => a.order >= proj.order);
        for(let pro of projects) {
          pro.order = pro.order + 1;
        }
        this.addNewProject(project, indent, proj.parent);
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

  getProjectById(projectId: number): ProjectTreeElem | undefined {
    return this.list.find((a) => a.id == projectId);
  }

  deleteProject(projectId: number) {
    this.list = this.list.filter((a) => a.id != projectId);
  }

  changeFav(response: Project) {
    let project = this.getProjectById(response.id);
    if(project) {
      project.favorite = response.favorite;
    }
  }

  filterProjects(text: string): ProjectTreeElem[] {
    return this.list.filter((a) => 
      a.name.toLowerCase().includes(text.toLowerCase())
    );
  }
}
