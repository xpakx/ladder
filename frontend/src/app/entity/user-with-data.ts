import { LabelDetails } from "./label-details";
import { ProjectDetails } from "./project-details";
import { TaskDetails } from "./task-details";

export interface UserWithData {
    username: string;
    id: number;
    projects: ProjectDetails[];
    tasks: TaskDetails[];
    labels: LabelDetails[];
}