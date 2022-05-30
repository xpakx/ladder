import { LabelDetails } from "../../label/dto/label-details";
import { ParentWithId } from "../../common/dto/parent-with-id";
import { ProjectWithNameAndId } from "src/app/project/dto/project-with-name-and-id";
import { UserMin } from "../../user/dto/user-min";

export interface TaskDetails {
    id: number;
    title: string;
    description: string;
    project: ProjectWithNameAndId | null;
    parent: ParentWithId | null;
    due: Date | null;
    timeboxed: boolean;
    completed: boolean;
    collapsed: boolean;
    archived: boolean;
    projectOrder: number;
    dailyViewOrder: number;
    labels: LabelDetails[];
    priority: number;
    modifiedAt: Date;
    assigned: UserMin | null;
}