import { Injectable } from '@angular/core';
import { Habit } from 'src/app/habit/dto/habit';
import { HabitDetails } from 'src/app/habit/dto/habit-details';
import { LabelDetails } from '../label/dto/label-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { MovableTreeService } from 'src/app/common/movable-tree-service';

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
      generalOrder: response.generalOrder,
      priority: response.priority,
      labels: labels
    });
    this.sort();
  }

  updateHabit(response: Habit, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []) {
    let habit = this.getById(response.id);
    if(habit) {
      habit.description = response.description;
      habit.title = response.title;
      habit.project = project ? project : null;
      habit.allowNegative = response.allowNegative;
      habit.allowPositive = response.allowPositive;
      habit.modifiedAt = new Date(response.modifiedAt);
      habit.generalOrder = response.generalOrder;
      habit.priority = response.priority;
      habit.labels = labels;
    }
  }

  deleteHabit(habitId: number) {
    this.list = this.list.filter((a) => a.id != habitId);
  }

  addNewHabitAfter(habit: Habit, afterId: number, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []) {
    let afterHabit = this.getById(afterId);
    if(afterHabit) {
      let hbt : HabitDetails = afterHabit;
      habit.generalOrder = hbt.generalOrder+1;
      let tasks = this.list
        .filter((a) => project ? a.project && a.project.id == project.id : !a.project)
        .filter((a) => a.generalOrder > hbt.generalOrder);
      for(let sibling of tasks) {
        sibling.generalOrder = sibling.generalOrder + 1;
      }
      this.addNewHabit(habit, project, labels);
    }
  }

  addNewHabitBefore(habit: Habit, beforeId: number, project: ProjectTreeElem | undefined, labels: LabelDetails[] = []) {
    let beforeHabit = this.getById(beforeId);
    if(beforeHabit) {
      let hbt : HabitDetails = beforeHabit;
      habit.generalOrder = hbt.generalOrder;
      let tasks = this.list
        .filter((a) => project ? a.project && a.project.id == project.id : !a.project)
        .filter((a) => a.generalOrder >= hbt.generalOrder);
      for(let sibling of tasks) {
        sibling.generalOrder = sibling.generalOrder + 1;
      }
      this.addNewHabit(habit, project, labels);
    }
  }

  getHabitsFromInbox(): HabitDetails[] {
    return this.list.filter((a) => 
      !a.project
    );
  }

  moveHabitToProject(habit: Habit, project: ProjectTreeElem | undefined) {
    let habitToMove = this.getById(habit.id);
    if(habitToMove) {
      let siblings = project ? this.getHabitsByProject(project.id) : this.getHabitsFromInbox();
      
      habitToMove.modifiedAt =  new Date(habit.modifiedAt);
      habitToMove.project = project ? project : null;
      habitToMove.generalOrder = siblings
        .map((a) => a.generalOrder)
        .reduce(((total, curr)=>Math.max(total, curr)), 0) + 1;
      this.sort();
    }
  }

  sync(habits: HabitDetails[]) {
    for(let habit of habits) {
      let habitWithId = this.getById(habit.id);
      if(habitWithId) {
        habitWithId.title = habit.title;
        habitWithId.description = habit.description;
        habitWithId.generalOrder = habit.generalOrder;
        habitWithId.project = habit.project;
        habitWithId.allowPositive = habit.allowPositive;
        habitWithId.allowNegative = habit.allowNegative;
        habitWithId.modifiedAt = new Date(habit.modifiedAt);
        habitWithId.priority = habit.priority;
        habitWithId.labels = habit.labels;
      } else {
        this.list.push(habit);
      }
    }
    this.sort();
  }

  updateHabitPriority(habit: Habit) {
    let habitToUpdate = this.getById(habit.id);
    if(habitToUpdate) {
      habitToUpdate.priority = habit.priority;
      habitToUpdate.modifiedAt =  new Date(habit.modifiedAt);
    }
  }
}