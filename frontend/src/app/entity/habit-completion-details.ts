import { HabitWithId } from "./habit-with-id";

export interface HabitCompletionDetails {
    id: number;
    date: Date;
    positive: boolean;
    habit: HabitWithId;
}