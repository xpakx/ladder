import { ProjectWithNameAndId } from "./project-with-name-and-id";
import { TaskWithChildren } from "./task-with-children";

export interface FullProjectTree {
    id: number;
    name: string;
    tasks: TaskWithChildren[];
    children: FullProjectTree[];
}