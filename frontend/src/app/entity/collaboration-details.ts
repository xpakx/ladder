import { ProjectWithCollaboration } from "./collaboration-with-project";

export interface CollaborationDetails {
    id: number;
    taskCompletionAllowed: boolean;
    editionAllowed: boolean;
    project: ProjectWithCollaboration;
}