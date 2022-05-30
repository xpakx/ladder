import { LabelDetails } from "src/app/label/dto/label-details";
import { ProjectWithNameAndId } from "src/app/project/dto/project-with-name-and-id";

export interface HabitDetails {
    title: string;
    description: string;
    id: number;
    generalOrder: number;
    project: ProjectWithNameAndId | null;
    allowPositive: boolean;
    allowNegative: boolean;
    modifiedAt: Date;
    priority: number;
    labels: LabelDetails[];
}