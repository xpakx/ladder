import { EntityWithId } from "../../common/dto/entity-with-id";

export interface LabelDetails extends EntityWithId {
    name: string;
    id: number;
    color: string;
    favorite: boolean;
    generalOrder: number;
    modifiedAt: Date;
}