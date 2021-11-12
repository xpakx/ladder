import { HabitDetails } from "./habit-details";
import { LabelDetails } from "./label-details";
import { ProjectDetails } from "./project-details";
import { TaskDetails } from "./task-details";

export interface SyncData {
    projects: ProjectDetails[];
    tasks: TaskDetails[];
    labels: LabelDetails[];
    habits: HabitDetails[];
}