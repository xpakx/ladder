import { ParentWithId } from "src/app/common/dto/parent-with-id";

export interface ProjectWithNameAndId extends ParentWithId {
    id: number;
    name: string;
}