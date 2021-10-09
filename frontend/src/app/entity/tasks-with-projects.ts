import { ProjectDetails } from "./project-details";
import { TaskDetails } from "./task-details";

export interface TasksWithProjects {
    projects: ProjectDetails[];
    tasks: TaskDetails[];
}