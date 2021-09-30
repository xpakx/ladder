import { IndentableTreeElem } from "./indentable-tree-elem";
import { LabelDetails } from "./label-details";
import { ParentWithId } from "./parent-with-id";
import { ProjectWithNameAndId } from "./project-with-name-and-id";

export interface TaskTreeElem extends IndentableTreeElem<ParentWithId> {
    id: number;
    title: string;
    description: string;
    project: ProjectWithNameAndId | null;
    parent: ParentWithId | null;
    due: Date | null;
    completed: boolean;
    order: number;

    realOrder: number;
    hasChildren: boolean;
    indent: number;
    parentList: TaskTreeElem[];
    collapsed: boolean;
    labels: LabelDetails[];
}