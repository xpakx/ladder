import { UserMin } from "src/app/user/dto/user-min";

export interface ProjectWithCollaboration {
    id: number;
    name: string;
    owner: UserMin;
}