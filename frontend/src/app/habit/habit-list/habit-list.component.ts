import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Habit } from '../dto/habit';
import { HabitCompletion } from '../dto/habit-completion';
import { HabitDetails } from '../dto/habit-details';
import { LabelDetails } from '../../label/dto/label-details';
import { ProjectTreeElem } from '../../project/dto/project-tree-elem';
import { AddEvent } from 'src/app/common/utils/add-event';
import { DeleteService } from 'src/app/utils/delete.service';
import { HabitCompletionTreeService } from 'src/app/habit/habit-completion-tree.service';
import { HabitTreeService } from 'src/app/habit/habit-tree.service';
import { HabitService } from 'src/app/habit/habit.service';
import { TreeService } from 'src/app/utils/tree.service';
import { DraggableComponent } from 'src/app/common/draggable-component';

@Component({
  selector: 'app-habit-list',
  templateUrl: './habit-list.component.html',
  styleUrls: ['./habit-list.component.css']
})
export class HabitListComponent 
extends DraggableComponent<HabitDetails, Habit, HabitService, HabitTreeService>
 implements OnInit {
  @Input("project") project: ProjectTreeElem | undefined;
  @Input("habitList") habitList: HabitDetails[] = [];
  @Input("blocked") blocked: boolean = false;

  showAddHabitForm: boolean = false;
  habitData: AddEvent<HabitDetails> = new AddEvent<HabitDetails>();

  constructor(public tree : TreeService, private router: Router,
    private renderer: Renderer2, private habitService: HabitService, 
    private deleteService: DeleteService, public treeService: HabitTreeService,
    public completions: HabitCompletionTreeService) {
      super(treeService, habitService);
     }

  ngOnInit(): void {
  }

  get habits(): HabitDetails[] {
    if(this.project && this.habitList.length == 0) {
      return this.treeService.getHabitsByProject(this.project.id);
    } else if(this.habitList.length > 0) {
      return this.habitList;
    } else {
      return [];
    }
  }

  get list(): boolean {
    return this.habitList.length > 0;
  }

  protected getElems(): HabitDetails[] {
    return this.habits;
  }

  public getPositive(habitId: number): number {
    return this.completions.countPositiveByHabitId(habitId);
  }

  public getNegative(habitId: number): number {
    return this.completions.countNegativeByHabitId(habitId);
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
      this.habitService.updateHabitProject({id: project? project.id : undefined}, this.taskIdForProjectModal).subscribe(
        (response: Habit, proj: ProjectTreeElem | undefined = project) => {
        this.tree.moveHabitToProject(response, proj);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
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
          this.tree.updateHabitPriority(response);
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

  openSelectPriorityModal(habit: HabitDetails) {
    this.taskIdForPriorityModal = habit.id;
    this.priorityForPriorityModal = habit.priority;
    this.showSelectPriorityModal = true;
  }

  openSelectPriorityModalFormContextMenu() {
    if(this.contextHabitMenu) {
      this.openSelectPriorityModal(this.contextHabitMenu);
    }
    this.closeContextHabitMenu();
  }

  getHabitLabels(habit: HabitDetails): LabelDetails[] {
    let labels: LabelDetails[] = [];
    for(let label of habit.labels) {
      let labelFromTree = this.tree.getLabelById(label.id);
      if(labelFromTree) {
        labels.push(labelFromTree);
      }
    }
    return labels;
  }

  duplicate() {
    if(this.contextHabitMenu) {
      let id = this.contextHabitMenu.id;
      this.habitService.duplicateHabit(this.contextHabitMenu.id).subscribe(
        (response: Habit, afterId: number = id, project = this.project ) => {
        this.tree.addNewHabitAfter(response, afterId, project, []);
      },
      (error: HttpErrorResponse) => {
       
      }
      );
    }

    this.closeContextHabitMenu();
  }

  getProjectColor(id: number): string {
    let project = this.tree.getProjectById(id)
    return project ? project.color : ""
  }

  completeHabit(id: number, positive: boolean) {
    let habit = this.tree.getHabitById(id);
    if(habit) {
    this.habitService.completeHabit(id, {flag: positive}).subscribe(
        (response: HabitCompletion) => {
        this.tree.completeHabit(id, response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
    }
  }
}
