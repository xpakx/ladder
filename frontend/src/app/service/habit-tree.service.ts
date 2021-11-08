import { Injectable } from '@angular/core';
import { Habit } from '../entity/habit';
import { HabitDetails } from '../entity/habit-details';
import { MovableTreeService } from './movable-tree-service';

@Injectable({
  providedIn: 'root'
})
export class HabitTreeService implements MovableTreeService<Habit> {
  public habits: HabitDetails[] = [];

  constructor() {
    this.habits = [
      {id: 1, title: "test", description: 'aaa', generalOrder: 1, project: {id: 67, name: ''}},
      {id: 2, title: "test2", description: '', generalOrder: 2, project: {id: 67, name: ''}}
    ]
   }

  load(habits: HabitDetails[] = []) {
    this.habits = habits;
    this.sort();
  }

  sort() {
    this.habits.sort((a, b) => a.generalOrder - b.generalOrder);
  }

  getHabits(): HabitDetails[] {
    return this.habits;
  }

  getHabitsByProject(projectId: number): HabitDetails[] {
    return this.habits.filter((a) => 
      a.project && a.project.id == projectId
    );
  }

  getById(id: number): HabitDetails | undefined {
    return this.habits.find((a) => a.id == id);
  }

  moveAfter(habit: Habit, afterId: number) {
    let afterLabel = this.getById(afterId);
    let movedLabel = this.getById(habit.id);
    if(afterLabel && movedLabel) {
      let lbl : HabitDetails = afterLabel;
      let labels = this.habits
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
      for(let lbl of this.habits) {
        lbl.generalOrder = lbl.generalOrder + 1;
      }
      movedLabel.generalOrder = 1;
      this.sort();
    }
  }
}
