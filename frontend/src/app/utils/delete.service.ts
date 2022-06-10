import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ProjectSearchListComponent } from 'src/app/project/project-search-list/project-search-list.component';
import { FilterDetails } from '../filter/dto/filter-details';
import { HabitDetails } from 'src/app/habit/dto/habit-details';
import { LabelDetails } from '../label/dto/label-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { TaskTreeElem } from '../task/dto/task-tree-elem';
import { CollabTaskService } from '../task/collab-task.service';
import { FilterService } from 'src/app/filter/filter.service';
import { HabitService } from 'src/app/habit/habit.service';
import { LabelService } from 'src/app/label/label.service';
import { ProjectService } from 'src/app/project/project.service';
import { TaskService } from 'src/app/task/task.service';
import { TreeService } from './tree.service';

@Injectable({
  providedIn: 'root'
})
export class DeleteService {
  private shouldDeleteLabel: boolean = false;
  private shouldDeleteProject: boolean = false;
  private shouldDeleteTask: boolean = false;
  private shouldDeleteCollabTask: boolean = false;
  private shouldDeleteHabit: boolean = false;
  private shouldDeleteFilter: boolean = false;
  private archived: boolean = false;

  public showDeleteMonit: boolean = false;
  public name: string = "";
  public id: number = -1;
  private archiveDelete: ProjectSearchListComponent | undefined;

  constructor(private labelService: LabelService, private projectService: ProjectService,
    private taskService: TaskService, private tree: TreeService, private habitService: HabitService,
    private filterService: FilterService, private collabService: CollabTaskService) { }

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
    } else if(this.shouldDeleteCollabTask) {
      this.deleteCollabTask(deletedId);
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

  openModalForArchivedProject(project: ProjectTreeElem, archive: ProjectSearchListComponent) {
     this.archived = true;
     this.archiveDelete = archive;
     this.openModalForProject(project);
  }

  private deleteProject(deletedId: number) {
    this.projectService.deleteProject(deletedId).subscribe(
      (response: any, projectId: number = deletedId, archived: boolean = this.archived) => {
      if(!archived) {
        this.tree.deleteProject(projectId)
      } else {
        if(this.archiveDelete) {
          this.archiveDelete.deleteProjectFromArchive(projectId);
        }
        this.archiveDelete = undefined;
        this.archived = false;
      }
    },
    (error: HttpErrorResponse) => {
      this.archiveDelete = undefined;
      this.archived = false;
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

  openModalForCollabTask(task: TaskTreeElem) {
    this.name = task.title;
    this.id = task.id;
    this.showDeleteMonit = true;
    this.shouldDeleteCollabTask = true;
  }

  private deleteCollabTask(deletedId: number) {
    this.collabService.deleteTask(deletedId).subscribe(
        (response: any, taskId: number = deletedId) => {
        this.tree.deleteCollabTask(taskId);
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

  isOpened(): boolean {
    return this.showDeleteMonit;
  }
}
