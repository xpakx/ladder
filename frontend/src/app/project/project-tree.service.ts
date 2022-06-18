import { Injectable } from '@angular/core';
import { Project } from 'src/app/project/dto/project';
import { ProjectDetails } from 'src/app/project/dto/project-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { ProjectWithNameAndId } from 'src/app/project/dto/project-with-name-and-id';
import { IndentableService } from 'src/app/common/indentable-service';
import { MultilevelMovableTreeService } from 'src/app/common/multilevel-movable-tree-service';

@Injectable({
  providedIn: 'root'
})
export class ProjectTreeService extends IndentableService<ProjectWithNameAndId>
implements MultilevelMovableTreeService<Project, ProjectTreeElem> {
  public list: ProjectTreeElem[] = [];
  private lastArchivization: Date | undefined;

  constructor() { super() }

  load(projects: ProjectDetails[]) {
    this.list = this.transformAll(projects);
    this.sort();
  }

  public getLastArchivization(): Date | undefined {
    return this.lastArchivization;
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
      modifiedAt: new Date(project.modifiedAt),
      collaborative: project.collaborative
    }
  }

  transformAndReturn(project: ProjectDetails): ProjectTreeElem {
    return {
      id: project.id,
      name: project.name,
      parent: project.parent,
      color: project.color,
      order: project.generalOrder,
      realOrder: project.generalOrder,
      hasChildren: false,
      indent: 0,
      parentList: [],
      favorite: project.favorite,
      collapsed: project.collapsed,
      modifiedAt: new Date(project.modifiedAt),
      collaborative: project.collaborative
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
      modifiedAt: new Date(project.modifiedAt),
      collaborative: false
    });
    this.sort();
  }

  addNewProjectAfter(project: Project, indent: number, afterId: number) {
    let afterProject = this.getById(afterId);
    if(afterProject) {
      project.order = afterProject.order+1;
      this.incrementOrderAfter(afterProject);
      this.addNewProject(project, indent, afterProject.parent);
    }
  }

  moveAfter(project: Project, afterId: number, indent: number = 0) {
    let afterProject = this.getById(afterId);
    let movedProject = this.getById(project.id);
    if(afterProject && movedProject) {
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getById(movedProject.parent.id) : undefined;
      this.incrementOrderAfter(afterProject);
      
      movedProject.indent = indent;
      movedProject.parent = afterProject.parent;
      movedProject.order = afterProject.order+1;

      this.recalculateChildrenIndent(movedProject.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(afterProject);

      this.sort();
    }
  }

  moveAsChild(project: Project, parentId: number, indent: number = 0) {
    let parentProject = this.getById(parentId);
    let movedProject = this.getById(project.id);
    if(parentProject && movedProject) {
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getById(movedProject.parent.id) : undefined;
      this.incrementOrderForAllSiblings(parentProject);
      
      movedProject.indent = indent;
      movedProject.order = 1;
      movedProject.parent = parentProject;

      this.recalculateChildrenIndent(movedProject.id, indent+1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }
      this.recalculateHasChildren(parentProject);

      this.sort();
    }
  }

  moveAsFirst(project: Project) {
    let movedProject = this.getById(project.id);
    if(movedProject) {
      let oldParent: ProjectTreeElem | undefined = movedProject.parent ? this.getById(movedProject.parent.id) : undefined;
      this.incrementOrdeForFirstOrderProjects();
      
      movedProject.indent = 0;
      movedProject.order = 1;
      movedProject.parent = null;

      this.recalculateChildrenIndent(movedProject.id, 1);
      if(oldParent) {
        this.recalculateHasChildren(oldParent);
      }

      this.sort();
    }
  }

  addNewProjectBefore(project: Project, indent: number, beforeId: number) {
    let beforeProject = this.getById(beforeId);
    if(beforeProject) {
      project.order = beforeProject.order;
      this.incrementOrderAfterOrEqual(beforeProject);
      this.addNewProject(project, indent, beforeProject.parent);
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

  archiveProject(response: Project) {
    let children = this.getAllFirstOrderChildren(response.id);
    let project = this.getById(response.id);
    let order = project ? project.order : 0;
    let parent = project ? project.parent : null;
    this.list = this.list.filter((a) => a.id != response.id);
    for(let child of children) {
      child.order = order++;
      child.parent = parent;
      child.indent = project? project.indent : 0;
    }

    if(parent) {
      this.recalculateChildrenIndent(parent.id, project ? project.indent : 0);
    }
    this.lastArchivization = new Date(response.modifiedAt);
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

  addDuplicated(response: ProjectDetails[], mainId: number) {
    let projects = this.transformAll(response);
    let mainProject = this.getById(mainId);
    if(mainProject) {
      this.incrementOrderAfter(mainProject);
    }
    this.list = this.list.concat(projects);
    this.sort();
  }

  incrementOrderForAllSiblings(parent: ProjectTreeElem) {
    let siblings = this.list
        .filter((a) => !a.parent && !parent || (a.parent && parent && a.parent.id == parent.id));
    for(let sibling of siblings) {
      sibling.order = sibling.order + 1;
    }
  }

  incrementOrdeForFirstOrderProjects() {
    let projects = this.list.filter((a) => !a.parent);
    for(let project of projects) {
      project.order = project.order + 1;
    }
  }
  
  incrementOrderAfter(project: ProjectTreeElem) {
    let siblingsAfter = this.list
        .filter((a) => !a.parent && !project.parent || (a.parent && project.parent && a.parent.id == project.parent.id))
        .filter((a) => a.order > project.order);
    for(let sibling of siblingsAfter) {
      sibling.order = sibling.order + 1;
    }
  }

  incrementOrderAfterOrEqual(project: ProjectTreeElem) {
    let siblingsAfter = this.list
        .filter((a) => !a.parent && !project.parent || (a.parent && project.parent && a.parent.id == project.parent.id))
        .filter((a) => a.order >= project.order);
    for(let sibling of siblingsAfter) {
      sibling.order = sibling.order + 1;
    }
  }

  sync(projects: ProjectDetails[]) {
    for(let project of projects) {
      let projectWithId = this.getById(project.id);
      if(projectWithId) {
        if(project.archived) {
          this.lastArchivization = new Date(project.modifiedAt);
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
      } else {
        this.lastArchivization = new Date(project.modifiedAt);
      }
    }
    this.sort();
  }

  syncOne(project: ProjectDetails):boolean {
    let oldProject = this.getById(project.id);
    if(!oldProject) {
      this.list.push(this.transformSync(project, []));
      return true;
    }
    return false;
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
      project.collaborative = details.collaborative;
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
      modifiedAt: new Date(project.modifiedAt),
      collaborative: project.collaborative
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
