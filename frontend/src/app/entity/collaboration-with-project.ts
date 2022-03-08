import { UserMin } from "./user-min";

export interface CollaborationWithProject {
    id: number;
    name: string;
    owner: UserMin;
}