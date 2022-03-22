import { TaskTreeElem } from "../task-tree-elem";

export interface Day {
    date: Date;
    id: number;
    tasks: TaskTreeElem[];
    collapsed: boolean;
}