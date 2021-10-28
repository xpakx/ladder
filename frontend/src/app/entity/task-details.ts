import { LabelDetails } from "./label-details";
import { ParentWithId } from "./parent-with-id";
import { ProjectWithNameAndId } from "./project-with-name-and-id";

export interface TaskDetails {
    id: number;
    title: string;
    description: string;
    project: ProjectWithNameAndId | null;
    parent: ParentWithId | null;
    due: Date | null;
    completed: boolean;
    collapsed: boolean;
    projectOrder: number;
    dailyViewOrder: number;
    labels: LabelDetails[];
    priority: number;
    modifiedAt: Date;
}