import { ProjectWithNameAndId } from "./project-with-name-and-id";

export interface ProjectDetails {
    id: number;
    name: string;
    parent: ProjectWithNameAndId | null;
    color: string;
    generalOrder: number;
    favorite: boolean;
    collapsed: boolean;
    modifiedAt: Date;
    archived: boolean;
    collaborative: boolean;
}