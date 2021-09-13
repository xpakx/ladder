import { Injectable } from '@angular/core';
import { LabelDetails } from '../entity/label-details';
import { Project } from '../entity/project';
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
  public loaded: boolean = false;

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
      },
      {
        id: 6,
        name: "Project 7", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 7,
        name: "Project 8", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 8,
        name: "Project 9", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 9,
        name: "Project 10", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 10,
        name: "Project 11", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 11,
        name: "Project 12", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 12,
        name: "Project 13", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 13,
        name: "Project 14", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 14,
        name: "Project 15", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 15,
        name: "Project 16", 
        parent: {id: 4, name: "Project 5"},
        color: "#DB357D",
        order: 0,
        realOrder: undefined
      },
      {
        id: 16,
        name: "Project 17", 
        parent: {id: 4, name: "Project 5"},
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
      }, 
      {
        id: 1,
        title: "Task 2",
        description: "Task with description",
        project: {id: 0, name: "Project 1"},
        parent: null,
        due: new Date(),
        completed: true
      }, 
      {
        id: 2,
        title: "Task 3",
        description: "",
        project: {id: 0, name: "Project 1"},
        parent: null,
        due: new Date(),
        completed: false
      }, 
      {
        id: 3,
        title: "Task 4",
        description: "",
        project: {id: 0, name: "Project 1"},
        parent: {id: 1},
        due: new Date(),
        completed: false
      }, 
      {
        id: 4,
        title: "Task 5",
        description: "",
        project: {id: 1, name: "Project 2"},
        parent: null,
        due: new Date(),
        completed: false
      }, 
      {
        id: 5,
        title: "Task 6",
        description: "",
        project: null,
        parent: null,
        due: new Date(),
        completed: false
      }
    ];
  }

  isLoaded(): boolean {
    return true; //this.loaded;
  }

  load(tree: UserWithData) {
    this.loaded = true;
    this.projects = this.transformAll(tree.projects);
    this.projects.sort((a, b) => a.order - b.order);
    this.calculateRealOrder();
    this.projects.sort((a, b) => a.realOrder - b.realOrder);
    this.tasks = tree.tasks;
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
      parentList: []
    });
    this.projects.sort((a, b) => a.order - b.order);
    this.calculateRealOrder();
    this.projects.sort((a, b) => a.realOrder - b.realOrder);
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
      indent: indent,
      parentList: []
    }
  }

  calculateRealOrder() {
    let proj = this.projects.filter((a) => a.indent == 0);
    var offset = 0;
    for(let project of proj) {
      offset += this.countAllChildren(project, offset) +1;
    }
  }

  countAllChildren(project: ProjectTreeElem, offset: number, parent?: ProjectTreeElem): number {
    project.realOrder = offset;
    offset += 1;
    
    if(parent) {
		project.parentList = [...parent.parentList];
		project.parentList.push(parent.id);
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
      a.due.getDate() === date.getDate() && a.due.getMonth() === date.getMonth() && a.due.getFullYear() === date.getFullYear() 
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
    return this.getByDate(new Date()).length;
  }

  getProjectById(projectId: number): ProjectTreeElem | undefined {
    return this.projects.find((a) => a.id == projectId);
  }

  getTasksByProject(projectId: number): TaskDetails[] {
    return this.tasks.filter((a) => 
      a.project && a.project.id == projectId
    );
  }
 
}
