import { IndentableTreeElem } from "src/app/common/dto/indentable-tree-elem";
import { LabelDetails } from "../../label/dto/label-details";
import { ParentWithId } from "../../common/dto/parent-with-id";
import { ProjectWithNameAndId } from "src/app/project/dto/project-with-name-and-id";
import { UserMin } from "../../user/dto/user-min";

export interface TaskTreeElem extends IndentableTreeElem<ParentWithId> {
    id: number;
    title: string;
    description: string;
    project: ProjectWithNameAndId | null;
    parent: ParentWithId | null;
    due: Date | null;
    timeboxed: boolean;
    completed: boolean;
    order: number;
    dailyOrder: number;

    realOrder: number;
    hasChildren: boolean;
    indent: number;
    parentList: TaskTreeElem[];
    collapsed: boolean;
    labels: LabelDetails[];
    priority: number;
    modifiedAt: Date;

    assigned: UserMin | null;
}