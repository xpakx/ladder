import { ParentWithId } from "./parent-with-id";
import { ProjectWithNameAndId } from "./project-with-name-and-id";

export interface TaskDetails {
    id: number;
    title: string;
    description: string;
    project: ProjectWithNameAndId;
    parent: ParentWithId | null;
    due: Date;
    completed: boolean;
}