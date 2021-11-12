import { ProjectWithNameAndId } from "./project-with-name-and-id";

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
}