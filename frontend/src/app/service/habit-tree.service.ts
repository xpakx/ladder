import { Injectable } from '@angular/core';
import { Habit } from '../entity/habit';
import { HabitDetails } from '../entity/habit-details';
import { LabelDetails } from '../entity/label-details';
import { ProjectTreeElem } from '../entity/project-tree-elem';
import { MovableTreeService } from './movable-tree-service';

@Injectable({
  providedIn: 'root'
})
export class HabitTreeService implements MovableTreeService<Habit> {
  public list: HabitDetails[] = [];

  constructor() {}

  load(habits: HabitDetails[] = []) {
    this.list = habits;
    this.sort();
  }

  sort() {
    this.list.sort((a, b) => a.generalOrder - b.generalOrder);
  }

  getHabits(): HabitDetails[] {
    return this.list;
  }

  getHabitsByProject(projectId: number): HabitDetails[] {
    return this.list.filter((a) => 
      a.project && a.project.id == projectId
    );
  }

  getById(id: number): HabitDetails | undefined {
    return this.list.find((a) => a.id == id);
  }

  moveAfter(habit: Habit, afterId: number) {
    let afterLabel = this.getById(afterId);
    let movedLabel = this.getById(habit.id);
    if(afterLabel && movedLabel) {
      let lbl : HabitDetails = afterLabel;
      let labels = this.list
        .filter((a) => a.generalOrder > lbl.generalOrder);
        for(let lab of labels) {
          lab.generalOrder = lab.generalOrder + 1;
        }
      
      movedLabel.generalOrder = afterLabel.generalOrder+1;

      this.sort();
    }
  }

  moveAsFirst(label: Habit) {
    let movedLabel = this.getById(label.id);
    if(movedLabel) {
      for(let lbl of this.list) {
        lbl.generalOrder = lbl.generalOrder + 1;
      }
      movedLabel.generalOrder = 1;
      this.sort();
    }
  }

  addNewHabit(response: Habit, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []) {
    this.list.push({
      id:response.id,
      title: response.title,
      description: response.description,
      project: project ? project : null,
      allowNegative: response.allowNegative,
      allowPositive: response.allowPositive,
      modifiedAt:  new Date(response.modifiedAt),
      generalOrder: response.generalOrder
    });
    this.sort();
  }
}
