import { CollabProjectData } from "./collab-project-data";
import { CollabProjectDetails } from "./collab-project-details";
import { CollabTaskDetails } from "./collab-task-details";
import { FilterDetails } from "./filter-details";
import { HabitCompletionDetails } from "./habit-completion-details";
import { HabitDetails } from "./habit-details";
import { LabelDetails } from "./label-details";
import { ProjectDetails } from "./project-details";
import { TaskDetails } from "./task-details";

export interface SyncData {
    projects: ProjectDetails[];
    tasks: TaskDetails[];
    labels: LabelDetails[];
    habits: HabitDetails[];
    habitCompletions: HabitCompletionDetails[];
    filters: FilterDetails[];
    collabs: CollabProjectData[];
    collabTasks: CollabTaskDetails[];
}