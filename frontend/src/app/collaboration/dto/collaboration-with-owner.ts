import { UserMin } from "src/app/user/dto/user-min";

export interface CollaborationWithOwner {
    id: number;
    taskCompletionAllowed: boolean;
    editionAllowed: boolean;
    owner: UserMin;
}