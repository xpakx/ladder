import { CollabProjectDetails } from "./collab-project-details";
import { CollabTaskDetails } from "./collab-task-details";
import { FilterDetails } from "./filter-details";
import { HabitCompletionDetails } from "./habit-completion-details";
import { HabitDetails } from "./habit-details";
import { LabelDetails } from "./label-details";
import { ProjectDetails } from "./project-details";
import { TaskDetails } from "./task-details";

export interface UserWithData {
    username: string;
    id: number;
    projectCollapsed: boolean;
    projects: ProjectDetails[];
    tasks: TaskDetails[];
    labels: LabelDetails[];
    habits: HabitDetails[];
    todayHabitCompletions: HabitCompletionDetails[];
    filters: FilterDetails[];
    collabs: CollabProjectDetails[];
    collabTasks: CollabTaskDetails[];
}