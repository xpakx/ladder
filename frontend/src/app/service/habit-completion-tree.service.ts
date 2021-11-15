import { Injectable } from '@angular/core';
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
}
