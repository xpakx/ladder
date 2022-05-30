import { HttpErrorResponse } from "@angular/common/http";
import { DndDropEvent } from "ngx-drag-drop";
import { EntityWithId } from "src/app/common/dto/entity-with-id";
import { MovableService } from "src/app/common/movable-service";
import { MovableTreeService } from "src/app/common/movable-tree-service";

export class DraggableComponent<R extends EntityWithId, T, S extends MovableService<T>, U extends MovableTreeService<T>>  {
    draggedId: number | undefined;
    
    constructor(protected treeService : U, private service: S) { }
  
    onDragStart(id: number): void {
        this.draggedId = id;
    }
  
    onDragEnd(): void {
        this.draggedId = undefined;
    }
  
    isDragged(id: number): boolean {
      return this.draggedId == id;
    }
    
    onDrop(event: DndDropEvent, target: R): void {
      let id = Number(event.data);
      
      this.service.moveAfter({id: target.id}, id).subscribe(
          (response: T, afterId: number = target.id) => {
          this.treeService.moveAfter(response, afterId);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  
    onDropFirst(event: DndDropEvent): void {
      let id = Number(event.data);
      this.service.moveAsFirst(id).subscribe(
          (response: T) => {
          this.treeService.moveAsFirst(response);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }
  