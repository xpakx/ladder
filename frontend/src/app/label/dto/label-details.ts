import { EntityWithId } from "../../entity/entity-with-id";

export interface LabelDetails extends EntityWithId {
    name: string;
    id: number;
    color: string;
    favorite: boolean;
    generalOrder: number;
    modifiedAt: Date;
}