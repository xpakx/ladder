import { ProjectDetails } from "src/app/project/dto/project-details";
import { TaskDetails } from "../../task/dto/task-details";

export interface TasksWithProjects {
    projects: ProjectDetails[];
    tasks: TaskDetails[];
}