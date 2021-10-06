import { Injectable } from '@angular/core';
import { Project } from '../entity/project';
import { ProjectDetails } from '../entity/project-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { ProjectWithNameAndId } from '../entity/project-with-name-and-id';
import { IndentableService } from './indentable-service';
import { MultilevelMovableTreeService } from './multilevel-movable-tree-service';

@Injectable({
  providedIn: 'root'
})
export class ProjectTreeService extends IndentableService<ProjectWithNameAndId>
implements MultilevelMovableTreeService<Project, ProjectTreeElem> {
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
      hasChildren: this.hasChildrenById(project.id, projects),
      indent: indent,
      parentList: [],
      favorite: project.favorite,
      collapsed: project.collapsed
    }
  }

  private hasChildrenById(projectId: number, projects: ProjectDetails[]): boolean {
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
    let afterProject = this.getById(afterId);
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

  moveAfter(project: Project, afterId: number, indent: number = 0) {
    let afterProject = this.getById(afterId);
    let movedProject = this.getById(project.id);
    if(afterProject && movedProject) {
      let proj : ProjectTreeElem = afterProject;
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getById(movedProject.parent.id) : undefined;
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

  moveAsChild(project: Project, parentId: number, indent: number = 0) {
    let parentProject = this.getById(parentId);
    let movedProject = this.getById(project.id);
    if(parentProject && movedProject) {
      let proj : ProjectTreeElem = parentProject;
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getById(movedProject.parent.id) : undefined;
      let projects = this.list
        .filter((a) => a.parent == proj);
        for(let pro of projects) {
          pro.order = pro.order + 1;
        }
      
      movedProject.indent = indent;
      movedProject.order = 1;
      movedProject.parent = parentProject;

      this.recalculateChildrenIndent(movedProject.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(proj);

      this.sort();
    }
  }

  moveAsFirst(project: Project) {
    let movedProject = this.getById(project.id);
    if(movedProject) {
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getById(movedProject.parent.id) : undefined;
      let projects = this.list.filter((a) => !a.parent);
        for(let pro of projects) {
          pro.order = pro.order + 1;
        }
      
      movedProject.indent = 0;
      movedProject.order = 1;
      movedProject.parent = null;

      this.recalculateChildrenIndent(movedProject.id, 2);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }

      this.sort();
    }
  }

  addNewProjectBefore(project: Project, indent: number, beforeId: number) {
    let beforeProject = this.getById(beforeId);
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
    let projectElem = this.getById(id)
    if(projectElem) {
      projectElem.name = project.name;
      projectElem.color = project.color;
      projectElem.favorite = project.favorite;
    }
  }

  getById(projectId: number): ProjectTreeElem | undefined {
    return this.list.find((a) => a.id == projectId);
  }

  getAll(): ProjectTreeElem[] {
    return this.list;
  }

  deleteProject(projectId: number): number[] {
    let ids = this.getAllChildren(projectId);
    this.list = this.list.filter((a) => !ids.includes(a.id));
    return ids;
  }

  protected getAllChildren(projectId: number): number[] {
    let children = this.list
    .filter((a) => a.parent && a.parent.id == projectId);
    let result: number[] = [];
    result.push(projectId);
    for(let child of children) {
        result = result.concat(this.getAllChildren(child.id));
    }
    return result;
  }

  changeFav(response: Project) {
    let project = this.getById(response.id);
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
