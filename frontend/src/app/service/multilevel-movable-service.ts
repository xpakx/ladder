import { Observable } from "rxjs";
import { BooleanRequest } from "../common/dto/boolean-request";
import { IdRequest } from "../common/dto/id-request";
import { MovableService } from "./movable-service";

export interface MultilevelMovableService<T> extends MovableService<T> {
    moveAsChild(request: IdRequest, taskId: number):  Observable<T> 
    updateCollapse(id: number, request: BooleanRequest):  Observable<T> 
}