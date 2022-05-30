import { EntityWithId } from "src/app/common/dto/entity-with-id";
import { ParentWithId } from "src/app/common/dto/parent-with-id";

export interface IndentableTreeElem<T extends ParentWithId> extends EntityWithId {
    id: number;
    parent: T | null;
    order: number;
    realOrder: number;
    hasChildren: boolean;
    indent: number;
    parentList: IndentableTreeElem<T>[];
    collapsed: boolean;
}