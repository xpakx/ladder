import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HabitDetails } from '../entity/habit-details';
import { LabelDetails } from '../entity/label-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { TaskTreeElem } from '../entity/task-tree-elem';
import { LabelService } from './label.service';
import { ProjectService } from './project.service';
import { TaskService } from './task.service';
import { TreeService } from './tree.service';

@Injectable({
  providedIn: 'root'
})
export class DeleteService {
  private shouldDeleteLabel: boolean = false;
  private shouldDeleteProject: boolean = false;
  private shouldDeleteTask: boolean = false;

  public showDeleteMonit: boolean = false;
  public name: string = "";
  public id: number = -1;

  constructor(private labelService: LabelService, private projectService: ProjectService,
    private taskService: TaskService, private tree: TreeService) { }

  delete(deletedId: number) {
    if(this.shouldDeleteLabel) {
      this.deleteLabel(deletedId);
    } else if(this.shouldDeleteProject) {
      this.deleteProject(deletedId);
    } else if(this.shouldDeleteTask) {
      this.deleteTask(deletedId);
    }

    this.closeModal();    
  }

  openModalForLabel(label: LabelDetails) {
    this.name = label.name;
    this.id = label.id;
    this.showDeleteMonit = true;
    this.shouldDeleteLabel = true;
  }

  private deleteLabel(deletedId: number) {
    this.labelService.deleteLabel(deletedId).subscribe(
        (response: any, labelId: number = deletedId) => {
        this.tree.deleteLabel(labelId);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  openModalForProject(project: ProjectTreeElem) {
    this.name = project.name;
    this.id = project.id;
    this.showDeleteMonit = true;
    this.shouldDeleteProject = true;
  }

  private deleteProject(deletedId: number) {
      this.projectService.deleteProject(deletedId).subscribe(
        (response: any, projectId: number = deletedId) => {
        this.tree.deleteProject(projectId);
      },
      (error: HttpErrorResponse) => {
       
      });
  }

  openModalForTask(task: TaskTreeElem) {
    this.name = task.title;
    this.id = task.id;
    this.showDeleteMonit = true;
    this.shouldDeleteTask = true;
  }

  private deleteTask(deletedId: number) {
    this.taskService.deleteTask(deletedId).subscribe(
        (response: any, taskId: number = deletedId) => {
        this.tree.deleteTask(taskId);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  openModalForHabit(habit: HabitDetails) {
    
  }

  closeModal() {
    this.showDeleteMonit = false;
    this.shouldDeleteLabel = false;
    this.shouldDeleteProject = false;
    this.shouldDeleteTask = false;
    this.name = "";
    this.id = -1;
  }
}
