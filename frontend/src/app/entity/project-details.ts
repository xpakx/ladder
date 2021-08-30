import { ProjectWithNameAndId } from "./project-with-name-and-id";

export interface ProjectDetails {
    id: number;
    name: string;
    parent: ProjectWithNameAndId;
}