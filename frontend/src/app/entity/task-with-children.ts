import { ProjectWithNameAndId } from "./project-with-name-and-id";

export interface TaskWithChildren {
    id: number;
    title: string;
    description: string;
    project: ProjectWithNameAndId;
    children: TaskWithChildren[];
}