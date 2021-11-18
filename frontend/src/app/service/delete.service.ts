import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FilterDetails } from '../entity/filter-details';
import { HabitDetails } from '../entity/habit-details';
import { LabelDetails } from '../entity/label-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { TaskTreeElem } from '../entity/task-tree-elem';
import { FilterService } from './filter.service';
import { HabitService } from './habit.service';
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
  private shouldDeleteHabit: boolean = false;
  private shouldDeleteFilter: boolean = false;

  public showDeleteMonit: boolean = false;
  public name: string = "";
  public id: number = -1;

  constructor(private labelService: LabelService, private projectService: ProjectService,
    private taskService: TaskService, private tree: TreeService, private habitService: HabitService,
    private filterService: FilterService) { }

  delete(deletedId: number) {
    if(this.shouldDeleteLabel) {
      this.deleteLabel(deletedId);
    } else if(this.shouldDeleteProject) {
      this.deleteProject(deletedId);
    } else if(this.shouldDeleteTask) {
      this.deleteTask(deletedId);
    } else if(this.shouldDeleteHabit) {
      this.deleteHabit(deletedId);
    } else if(this.shouldDeleteFilter) {
      this.deleteFilter(deletedId);
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
    this.name = habit.title;
    this.id = habit.id;
    this.showDeleteMonit = true;
    this.shouldDeleteHabit = true;
  }

  private deleteHabit(deletedId: number) {
    this.habitService.deleteHabit(deletedId).subscribe(
        (response: any, taskId: number = deletedId) => {
        this.tree.deleteHabit(taskId);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  openModalForFilter(filter: FilterDetails) {
    this.name = filter.name;
    this.id = filter.id;
    this.showDeleteMonit = true;
    this.shouldDeleteFilter = true;
  }

  private deleteFilter(deletedId: number) {
    this.filterService.deleteFilter(deletedId).subscribe(
        (response: any, filterId: number = deletedId) => {
        this.tree.deleteFilter(filterId);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
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
