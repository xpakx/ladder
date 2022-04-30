import { HttpErrorResponse } from "@angular/common/http";
import { DndDropEvent } from "ngx-drag-drop";
import { ParentWithId } from "src/app/entity/parent-with-id";
import { ProjectTreeElem } from "src/app/entity/project-tree-elem";
import { Task } from "src/app/entity/task";
import { TaskTreeElem } from "src/app/entity/task-tree-elem";
import { CollabTaskService } from "src/app/service/collab-task.service";
import { MovableTaskTreeService } from "src/app/service/movable-task-tree-service";
import { MultilevelDraggableComponent } from "./multilevel-draggable-component";

export class MultilevelCollabTaskComponent<U extends MovableTaskTreeService<Task, TaskTreeElem>>
extends MultilevelDraggableComponent<ParentWithId, TaskTreeElem, Task, CollabTaskService, U>  {
    project: ProjectTreeElem | undefined;
    
    constructor(protected treeService : U, protected service: CollabTaskService) { 
      super(treeService, service) 
    }
    
    onDropFirst(event: DndDropEvent): void {
        let id = Number(event.data);
        this.service.moveAsFirst(id).subscribe(
          (response: Task, project: ProjectTreeElem | undefined = this.project) => {
          this.treeService.moveTaskAsFirst(response, project);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }   

    getListForDropzones(i: number, elem: TaskTreeElem): TaskTreeElem[] {
      let dropzones = elem.indent - this.amountOfDropzones(i, elem);
      return elem.parentList.slice(-dropzones);
  }
}
  