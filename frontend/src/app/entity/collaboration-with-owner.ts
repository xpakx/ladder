import { UserMin } from "../user/user-min";

export interface CollaborationWithOwner {
    id: number;
    taskCompletionAllowed: boolean;
    editionAllowed: boolean;
    owner: UserMin;
}