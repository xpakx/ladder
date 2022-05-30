import { UserMin } from "../user/user-min";

export interface ProjectWithCollaboration {
    id: number;
    name: string;
    owner: UserMin;
}