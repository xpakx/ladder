import { ParentWithId } from "./parent-with-id";

export interface IndentableTreeElem<T extends ParentWithId> {
    id: number;
    parent: T | null;
    order: number;
    realOrder: number;
    hasChildren: boolean;
    indent: number;
    parentList: IndentableTreeElem<T>[];
}