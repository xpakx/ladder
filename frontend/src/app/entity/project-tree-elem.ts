import { IndentableTreeElem } from "./indentable-tree-elem";
import { ProjectWithNameAndId } from "./project-with-name-and-id";

export interface ProjectTreeElem extends IndentableTreeElem<ProjectWithNameAndId> {
    name: string;
    parent: ProjectWithNameAndId | null;
    color: string;
    order: number;
    realOrder: number;
    hasChildren: boolean;
    indent: number;
    parentList: ProjectTreeElem[];
    favorite: boolean;
    collapsed: boolean;
}
