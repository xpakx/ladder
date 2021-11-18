import { Injectable } from '@angular/core';
import { HabitCompletion } from '../entity/habit-completion';
import { HabitCompletionDetails } from '../entity/habit-completion-details';

@Injectable({
  providedIn: 'root'
})
export class HabitCompletionTreeService {
  public list: HabitCompletionDetails[] = [];

  constructor() { }

  load(completions: HabitCompletionDetails[] = []) {
    this.list = completions;
  }

  countPositiveByHabitId(id: number) {
    return this.list.filter((a) => 
      a.habit.id == id && a.positive
    ).length;
  }

  countNegativeByHabitId(id: number) {
    return this.list.filter((a) => 
      a.habit.id == id && !a.positive
    ).length;
  }

  addCompletion(habitId: number, completion: HabitCompletion) {
    this.list.push({
      id: completion.id,
      date: completion.date,
      positive: completion.positive,
      habit: {id: habitId}
    });
  }

  sync(completions: HabitCompletionDetails[]) {
    this.list = this.list.concat(completions);
  }
}
