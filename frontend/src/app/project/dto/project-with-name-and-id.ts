import { ParentWithId } from "src/app/entity/parent-with-id";

export interface ProjectWithNameAndId extends ParentWithId {
    id: number;
    name: string;
}