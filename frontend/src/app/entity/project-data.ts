import { HabitCompletionDetails } from "./habit-completion-details";
import { HabitDetails } from "./habit-details";
import { ProjectDetails } from "./project-details";
import { TaskDetails } from "./task-details";

export interface ProjectData {
    project: ProjectDetails;
    tasks: TaskDetails[];
    habits: HabitDetails[];
    //habitCompletions: HabitCompletionDetails[];
}