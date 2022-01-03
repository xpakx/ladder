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
      collapsed: project.collapsed,
      modifiedAt: new Date(project.modifiedAt)
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
      collapsed: true,
      modifiedAt: new Date(project.modifiedAt)
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

  archiveProject(projectId: number) {
    let children = this.getAllFirstOrderChildren(projectId);
    let project = this.getById(projectId);
    let order = project ? project.order : 0;
    let parent = project ? project.parent : null;
    this.list = this.list.filter((a) => a.id != projectId);
    for(let child of children) {
      child.order = order++;
      child.parent = parent;
    }

    if(parent) {
      this.recalculateChildrenIndent(parent.id, project ? project.indent : 0);
    }
    this.sort();
  }

  protected getAllFirstOrderChildren(projectId: number): ProjectTreeElem[] {
    return this.list
    .filter((a) => a.parent && a.parent.id == projectId);
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

  addDuplicated(response: ProjectDetails[]) {
    let projects = this.transformAll(response);
    this.list = this.list.concat(projects);
    this.sort();
  }

  sync(projects: ProjectDetails[]) {
    for(let project of projects) {
      let projectWithId = this.getById(project.id);
      if(projectWithId) {
        if(project.archived) {
          let children = this.getAllFirstOrderChildren(project.id);
          let order = projectWithId.order;
          let parent = projectWithId.parent ? projectWithId.parent : null;
          this.list = this.list.filter((a) => a.id != project.id);
          for(let child of children) {
            child.order = order++;
            child.parent = parent;
          }
        } else {
          this.updateProjectDetails(projectWithId, project, projects);
        }
      } else if(!project.archived) {
        this.list.push(this.transformSync(project, projects));
      }
    }
    this.sort();
  }

  updateProjectDetails(project: ProjectTreeElem, details: ProjectDetails, projects: ProjectDetails[]) {
      project.name = details.name;
      project.color = details.color;
      project.favorite = details.favorite;
      project.order = details.generalOrder;
      project.favorite = details.favorite;
      project.collapsed = details.collapsed;
      project.modifiedAt = new Date(details.modifiedAt);
      project.parent = details.parent;
      let oldParent = project.parent ? this.getById(project.parent.id) : null;
      let newParent = project.parent ? this.getById(project.parent.id) : null;
      project.indent = newParent ? newParent.indent+1 : 0;
      this.recalculateChildrenIndent(project.id, project.indent+1);
      if(oldParent) {
        this.recalculateHasChildrenSync(oldParent, projects);
      }
      this.recalculateHasChildrenSync(project, projects);
  }

  private transformSync(project: ProjectDetails, projects: ProjectDetails[]): ProjectTreeElem {
    let indent: number = this.getIndentSync(project.id, projects);
    return {
      id: project.id,
      name: project.name,
      parent: project.parent,
      color: project.color,
      order: project.generalOrder,
      realOrder: project.generalOrder,
      hasChildren: this.hasChildrenByIdSync(project.id, projects),
      indent: indent,
      parentList: [],
      favorite: project.favorite,
      collapsed: project.collapsed,
      modifiedAt: new Date(project.modifiedAt)
    }
  }

  private hasChildrenByIdSync(projectId: number, projects: ProjectDetails[]): boolean {
    return this.hasChildrenById(projectId, projects) || this.list.find((a) => a.parent?.id == projectId) != null;
  }

  private findParentByIdSync(projectId: number, projects: ProjectDetails[]): number | undefined {
    let syncDataId = projects.find((a) => a.id == projectId)?.parent?.id;
    if(syncDataId) {return syncDataId;}
    return this.list.find((a) => a.id == projectId)?.parent?.id;
  }
  private getIndentSync(projectId: number, projects: ProjectDetails[]): number {
    let parentId: number | undefined = this.findParentByIdSync(projectId, projects);
    let counter = 0;
    while(parentId != null) {
      counter +=1;
      parentId = this.findParentByIdSync(parentId, projects);
    }
    return counter;
  }

  private howManyChildrenSync(projectId: number, projects: ProjectDetails[]) {
    let syncChildren = projects.filter((a) => a.parent && a.parent.id == projectId);
    let ids = syncChildren.map((a) => a.id);
    let children = this.list.filter((a) => a.parent && a.parent.id == projectId && !ids.includes(a.id));
    return children.length + syncChildren.length;
  }

  private recalculateHasChildrenSync(project: ProjectTreeElem, projects: ProjectDetails[]) {
      let children = this.howManyChildrenSync(project.id, projects);
      project.hasChildren = children > 0 ? true : false;
      for(let parent of project.parentList) {
          let parentChildren = this.howManyChildrenSync(parent.id, projects);
          parent.hasChildren = parentChildren > 0 ? true : false;
      }
  }
}
