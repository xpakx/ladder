import { UserMin } from "./user-min";

export interface TaskCommentDetails {
    id: number;
    content: string;
    createdAt: Date;
    owner: UserMin;
}