import { UserMin } from "src/app/user/dto/user-min";

export interface TaskCommentDetails {
    id: number;
    content: string;
    createdAt: Date;
    owner: UserMin;
}