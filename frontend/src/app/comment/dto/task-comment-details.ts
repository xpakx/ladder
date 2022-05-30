import { UserMin } from "src/app/user/user-min";

export interface TaskCommentDetails {
    id: number;
    content: string;
    createdAt: Date;
    owner: UserMin;
}