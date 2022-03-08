import { UserMin } from "./user-min";

export interface ProjectWithCollaboration {
    id: number;
    name: string;
    owner: UserMin;
}