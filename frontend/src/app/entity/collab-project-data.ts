import { CollabProjectDetails } from "./collab-project-details";

export interface CollabProjectData {
    id: number;
    taskCompletionAllowed: boolean;
    editionAllowed: boolean;
    project: CollabProjectDetails;
    modifiedAt: Date;
}