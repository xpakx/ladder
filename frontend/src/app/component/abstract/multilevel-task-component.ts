import { HttpErrorResponse } from "@angular/common/http";
import { DndDropEvent } from "ngx-drag-drop";
import { IndentableTreeElem } from "src/app/entity/indentable-tree-elem";
import { ParentWithId } from "src/app/entity/parent-with-id";
import { ProjectTreeElem } from "src/app/entity/project-tree-elem";
import { MovableTaskTreeService } from "src/app/service/movable-task-tree-service";
import { MultilevelMovableService } from "src/app/service/multilevel-movable-service";
import { MultilevelDraggableComponent } from "./multilevel-draggable-component";

export class MultilevelTaskComponent<P extends ParentWithId, R extends IndentableTreeElem<P>, T, S extends MultilevelMovableService<T>, U extends MovableTaskTreeService<T, R>>
extends MultilevelDraggableComponent<P, R, T, S, U>  {
    project: ProjectTreeElem | undefined;
    
    constructor(protected treeService : U, protected service: S) { super(treeService, service) }
    
    onDropFirst(event: DndDropEvent) {
        let id = Number(event.data);
        this.service.moveAsFirst(id).subscribe(
          (response: T, project: ProjectTreeElem | undefined = this.project) => {
          this.treeService.moveTaskAsFirst(response, project);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }    
}
  