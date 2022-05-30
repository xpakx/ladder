import { Injectable } from '@angular/core';
import { HabitCompletion } from 'src/app/habit/dto/habit-completion';
import { HabitCompletionDetails } from '../habit/dto/habit-completion-details';

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
      date: new Date(completion.date),
      positive: completion.positive,
      habit: {id: habitId}
    });
  }

  private containsId(completionId: number): boolean {
    return (this.list.find((a) => a.id == completionId)) ? true : false;
  }

  sync(completions: HabitCompletionDetails[]) {
    for(let completion of completions) {
      if(!this.containsId(completion.id)) {
        completion.date = new Date(completion.date);
        this.list.push(completion);
      }
    }
  }
}
