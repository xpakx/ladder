import { ParentWithId } from "./parent-with-id";
import { ProjectWithNameAndId } from "./project-with-name-and-id";
import { UserMin } from "./user-min";

export interface CollabTaskDetails {
    id: number;
    title: string;
    description: string;
    project: ProjectWithNameAndId | null;
    parent: ParentWithId | null;
    due: Date | null;
    completed: boolean;
    collapsed: boolean;
    archived: boolean;
    projectOrder: number;
    priority: number;
    modifiedAt: Date;
    assigned: UserMin | null;
}