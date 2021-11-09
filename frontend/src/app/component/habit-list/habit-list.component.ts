import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Habit } from 'src/app/entity/habit';
import { HabitDetails } from 'src/app/entity/habit-details';
import { LabelDetails } from 'src/app/entity/label-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { HabitTreeService } from 'src/app/service/habit-tree.service';
import { HabitService } from 'src/app/service/habit.service';
import { TreeService } from 'src/app/service/tree.service';
import { DraggableComponent } from '../abstract/draggable-component';

@Component({
  selector: 'app-habit-list',
  templateUrl: './habit-list.component.html',
  styleUrls: ['./habit-list.component.css']
})
export class HabitListComponent 
extends DraggableComponent<HabitDetails, Habit, HabitService, HabitTreeService>
 implements OnInit {
  @Input("project") project: ProjectTreeElem | undefined;

  showAddHabitForm: boolean = false;
  habitData: AddEvent<HabitDetails> = new AddEvent<HabitDetails>();

  constructor(public tree : TreeService, private router: Router,
    private renderer: Renderer2, private habitService: HabitService, 
    private deleteService: DeleteService, public treeService: HabitTreeService) {
      super(treeService, habitService);
     }

  ngOnInit(): void {
  }

  get habits(): HabitDetails[] {
    return this.project ? this.treeService.getHabitsByProject(this.project.id) : [];
  }

  protected getElems(): HabitDetails[] {
    return this.habits;
  }

  // Habit form
  openAddHabitForm() {
    this.closeEditHabitForm();
    this.showAddHabitForm = true;
    this.habitData = new AddEvent<HabitDetails>();
  }

  closeAddHabitForm() {
    this.showAddHabitForm = false;
  }

  openEditHabitForm(habit: HabitDetails) {
    this.closeAddHabitForm();
    this.habitData = new AddEvent<HabitDetails>(habit);
  }

  closeEditHabitForm() {
    this.habitData = new AddEvent<HabitDetails>();
  }

  openEditTaskFromContextMenu() {
    if(this.contextHabitMenu) {
      this.closeAddHabitForm();
      this.habitData = new AddEvent<HabitDetails>(this.contextHabitMenu);
    }
    this.closeContextHabitMenu();
  }

  openEditTaskAbove() {
    if(this.contextHabitMenu) {
      this.closeAddHabitForm();
      this.habitData = new AddEvent<HabitDetails>(this.contextHabitMenu, false, true);
    }
    this.closeContextHabitMenu();
  }

  openEditTaskBelow() {
    if(this.contextHabitMenu) {
      this.closeAddHabitForm();
      this.habitData = new AddEvent<HabitDetails>(this.contextHabitMenu, true, false);
    }
    this.closeContextHabitMenu();
  }

  shouldAddHabitBelowById(taskId: number): boolean {
    return this.habitObjectContains(taskId) && this.habitData.after;
  }

  shouldAddHabitAboveById(taskId: number): boolean {
    return this.habitObjectContains(taskId) && this.habitData.before;
  }

  shouldEditHabitById(habitId: number): boolean {
    return this.habitObjectContains(habitId) && this.habitData.isInEditMode();
  }

  habitObjectContains(habitId: number) {
    return habitId == this.habitData.object?.id;
   }

   contextHabitMenu: HabitDetails | undefined;
   showContextHabitMenu: boolean = false;
   contextHabitMenuJustOpened: boolean = false;
   habitContextMenuX: number = 0;
   habitContextMenuY: number = 0;
   @ViewChild('taskContext', {read: ElementRef}) taskContextMenuElem!: ElementRef;
 
 
   ngAfterViewInit() {
     this.renderer.listen('window', 'click',(e:Event)=>{
       if(this.showContextHabitMenu && 
         !this.taskContextMenuElem.nativeElement.contains(e.target)){
         if(this.contextHabitMenuJustOpened) {
           this.contextHabitMenuJustOpened = false
         } else {
           this.showContextHabitMenu = false;
         }
       }
     })
   }
 
   openContextHabitMenu(event: MouseEvent, habitId: number) {
     this.contextHabitMenu = this.treeService.getById(habitId);
     this.showContextHabitMenu = true;
     this.contextHabitMenuJustOpened = true;
     this.habitContextMenuX = event.clientX-250;
     this.habitContextMenuY = event.clientY;
   }
 
   closeContextHabitMenu() {
     this.contextHabitMenu = undefined;
     this.showContextHabitMenu = false;
   }

  askForDelete() {
    if(this.contextHabitMenu) {
      this.deleteService.openModalForHabit(this.contextHabitMenu);
    }
    this.closeContextHabitMenu();
  }


  showSelectProjectModal: boolean = false;
  projectForProjectModal: ProjectTreeElem | undefined;
  taskIdForProjectModal: number | undefined;

  closeSelectProjectModal(project: ProjectTreeElem | undefined) {
    this.showSelectProjectModal = false;
    if(this.taskIdForProjectModal) {
      //todo
    }
    this.projectForProjectModal = undefined;
    this.taskIdForProjectModal = undefined;
  }

  cancelProjectSelection() {
    this.showSelectProjectModal = false;
    this.projectForProjectModal = undefined;
    this.taskIdForProjectModal = undefined;
  }

  openSelectProjectModal(task: HabitDetails) {
    this.taskIdForProjectModal = task.id;
    this.projectForProjectModal = this.project;
    this.showSelectProjectModal = true;
  }

  openSelectProjectModalFormContextMenu() {
    if(this.contextHabitMenu) {
      this.openSelectProjectModal(this.contextHabitMenu);
    }
    this.closeContextHabitMenu();
  }

  showSelectPriorityModal: boolean = false;
  priorityForPriorityModal: number = 0;
  taskIdForPriorityModal: number | undefined;

  closeSelectPriorityModal(priority: number) {
    this.showSelectPriorityModal = false;
    if(this.taskIdForPriorityModal) {
      this.habitService.updateHabitPriority({priority: priority}, this.taskIdForPriorityModal).subscribe(
          (response: Habit) => {
          //this.tree.updateTaskPriority(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
  }

  cancelPrioritySelection() {
    this.showSelectPriorityModal = false;
    this.priorityForPriorityModal = 0;
    this.taskIdForPriorityModal = undefined;
  }

  openSelectPriorityModal(task: HabitDetails) {
    this.taskIdForPriorityModal = task.id;
    //this.priorityForPriorityModal = task.priority;
    this.showSelectPriorityModal = true;
  }

  openSelectPriorityModalFormContextMenu() {
    if(this.contextHabitMenu) {
      this.openSelectPriorityModal(this.contextHabitMenu);
    }
    this.closeContextHabitMenu();
  }

  getTaskLabels(habit: HabitDetails): LabelDetails[] {
    let labels: LabelDetails[] = [];
    /*for(let label of task.labels) {
      let labelFromTree = this.tree.getLabelById(label.id);
      if(labelFromTree) {
        labels.push(labelFromTree);
      }
    }*/
    return labels;
  }

  duplicate() {
    if(this.contextHabitMenu) {
      //todo
    }

    this.closeContextHabitMenu();
  }

}
