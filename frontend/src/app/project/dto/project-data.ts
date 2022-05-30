import { HabitDetails } from "src/app/habit/dto/habit-details";
import { ProjectDetails } from "./project-details";
import { TaskDetails } from "src/app/task/dto/task-details";

export interface ProjectData {
    project: ProjectDetails;
    tasks: TaskDetails[];
    habits: HabitDetails[];
    //habitCompletions: HabitCompletionDetails[];
}