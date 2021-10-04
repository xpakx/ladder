import { LabelDetails } from "../label-details";

export interface AddLabelEvent {
    label: LabelDetails | undefined;
    after: boolean;
    before: boolean;
}