import { ProjectWithNameAndId } from "./project-with-name-and-id";
import { UserMin } from "./user-min";

export interface CollaborationWithOwner {
    id: number;
    taskCompletionAllowed: boolean;
    editionAllowed: boolean;
    owner: UserMin;
}