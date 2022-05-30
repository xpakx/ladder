import { CollabProjectData } from "../project/dto/collab-project-data";
import { CollabTaskDetails } from "../task/dto/collab-task-details";
import { FilterDetails } from "../filter/dto/filter-details";
import { HabitCompletionDetails } from "../habit/dto/habit-completion-details";
import { HabitDetails } from "src/app/habit/dto/habit-details";
import { LabelDetails } from "../label/dto/label-details";
import { ProjectDetails } from "src/app/project/dto/project-details";
import { TaskDetails } from "../task/dto/task-details";

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
    collabs: CollabProjectData[];
    collabTasks: CollabTaskDetails[];
}