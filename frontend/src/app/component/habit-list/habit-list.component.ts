import { Component, Input, OnInit, Renderer2 } from '@angular/core';
import { Router } from '@angular/router';
import { Habit } from 'src/app/entity/habit';
import { HabitDetails } from 'src/app/entity/habit-details';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
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

}
