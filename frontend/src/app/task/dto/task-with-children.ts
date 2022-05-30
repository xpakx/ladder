import { ProjectWithNameAndId } from "src/app/project/dto/project-with-name-and-id";

export interface TaskWithChildren {
    id: number;
    title: string;
    description: string;
    project: ProjectWithNameAndId;
    children: TaskWithChildren[];
    due: Date;
    timeboxed: boolean;
}