import { Observable } from "rxjs";
import { IdRequest } from "../common/dto/id-request";

export interface MovableService<T> {
    moveAfter(request: IdRequest, taskId: number):  Observable<T> 
    moveAsFirst(taskId: number):  Observable<T> 
}