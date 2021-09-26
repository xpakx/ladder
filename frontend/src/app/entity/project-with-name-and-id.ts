import { ParentWithId } from "./parent-with-id";

export interface ProjectWithNameAndId extends ParentWithId {
    id: number;
    name: string;
}