import { ProjectWithNameAndId } from "./project-with-name-and-id";

export interface ProjectTreeElem {
    id: number;
    name: string;
    parent: ProjectWithNameAndId | null;
    color: string;
    order: number;
    realOrder: number;
    hasChildren: boolean;
    indent: number;
    parentList: number[];
}
