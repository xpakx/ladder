import { HttpErrorResponse } from "@angular/common/http";
import { DndDropEvent } from "ngx-drag-drop";
import { ParentWithId } from "src/app/entity/parent-with-id";
import { ProjectTreeElem } from "src/app/entity/project-tree-elem";
import { Task } from "src/app/entity/task";
import { TaskTreeElem } from "src/app/entity/task-tree-elem";
import { MovableTaskTreeService } from "src/app/service/movable-task-tree-service";
import { TaskService } from "src/app/service/task.service";
import { MultilevelDraggableComponent } from "./multilevel-draggable-component";

export class MultilevelTaskComponent<U extends MovableTaskTreeService<Task, TaskTreeElem>>
extends MultilevelDraggableComponent<ParentWithId, TaskTreeElem, Task, TaskService, U>  {
    project: ProjectTreeElem | undefined;
    
    constructor(protected treeService : U, protected service: TaskService) { 
      super(treeService, service) 
    }
    
    onDropFirst(event: DndDropEvent) {
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

  collapseInTree(elem: Task) {
        this.treeService.collapse(elem);
  } 
}
