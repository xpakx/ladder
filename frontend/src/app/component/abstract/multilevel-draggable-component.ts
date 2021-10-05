import { HttpErrorResponse } from "@angular/common/http";
import { DndDropEvent } from "ngx-drag-drop";
import { IndentableTreeElem } from "src/app/entity/indentable-tree-elem";
import { ParentWithId } from "src/app/entity/parent-with-id";
import { MultilevelMovableService } from "src/app/service/multilevel-movable-service";
import { MultilevelMovableTreeService } from "src/app/service/multilevel-movable-tree-service";

export class MultilevelDraggableComponent<P extends ParentWithId, R extends IndentableTreeElem<P>, T, S extends MultilevelMovableService<T>, U extends MultilevelMovableTreeService<T, R>>  {
    draggedId: number | undefined;
    
    constructor(protected treeService : U, private service: S) { }
  
    onDragStart(id: number) { 
        this.draggedId = id;
    }
  
    onDragEnd() {
        this.draggedId = undefined;
    }
  
    isDragged(id: number): boolean {
        return this.draggedId == id;
    }

    onDrop(event: DndDropEvent, target: IndentableTreeElem<P> /*?*/, asChild: boolean = false) { //override
    let id = Number(event.data);
        if(!asChild)
        {
            this.service.moveAfter({id: target.id}, id).subscribe(
                (response: T, indent: number = target.indent, afterId: number = target.id) => {
                    this.treeService.moveAfter(response, indent, afterId);
                },
                (error: HttpErrorResponse) => {
                
                }
            );
        } else {
            this.service.moveAsChild({id: target.id}, id).subscribe(
                (response: T, indent: number = target.indent+1, afterId: number = target.id) => {
                    this.treeService.moveAsChild(response, indent, afterId);
                },
                (error: HttpErrorResponse) => {
                
                }
            );
        }
    }

    
    onDropFirst(event: DndDropEvent) {
        let id = Number(event.data);
        this.service.moveAsFirst(id).subscribe(
            (response: T) => {
            this.treeService.moveAsFirst(response);
          },
          (error: HttpErrorResponse) => {
          
          }
        );
    }

    hideDropZone(elem: R): boolean {
        return this.isDragged(elem.id) || 
        this.isParentDragged(elem.parentList) || 
        this.isParentCollapsed(elem.parentList);
    }


    collapseProject(elemId: number) {
        let elem = this.treeService.getById(elemId);
        if(elem) {
          elem.collapsed = !elem.collapsed;
          this.service.updateCollapse(elem.id, {flag: elem.collapsed}).subscribe(
            (response: T) => {
            },
            (error: HttpErrorResponse) => {
            
            }
          );
        }
    }

    isParentDragged(elems: IndentableTreeElem<P>[]): boolean {
        for(let elem of elems) {
        if(elem.id == this.draggedId) {
          return true;
        }
        }
        return false;
    }
      
    isProjectCollapsed(elemId: number): boolean {
        let elem = this.treeService.getById(elemId);
        if(elem) {
          return elem.collapsed ? true : false;
        }
          return false;
    }
      
    isParentCollapsed(elems: IndentableTreeElem<P>[]): boolean {
        return elems.find((a) => a.collapsed) ? true : false;
    }

    getListForDropzones(i: number, elem: R) {
        let dropzones = elem.indent - this.amountOfDropzones(i, elem);
        return elem.parentList.slice(-dropzones);
    }
    
    protected amountOfDropzones(i: number, elem: R): number {
        return elem.hasChildren ?
          this.findFirstWithSmallerIndentAndReturnIndent(i + 1, elem.indent) : this.indentForPosition(i + 1);
    }
    
    getAmountOfNormalDropzones(i: number, elem: R): number {
        return this.amountOfDropzones(i, elem);
    }
    
    
    calculateAdditionalDropzones(i: number, elem: R): boolean {
        if(!elem.hasChildren) {
         return elem.indent > this.indentForPosition(i+1);
        } 
        if(!elem.collapsed) {
          return false;
        }
        return this.findFirstWithSmallerIndentAndReturnIndent(i+1, elem.indent) < elem.indent;
    }
    
    private findFirstWithSmallerIndentAndReturnIndent(index: number, indent: number): number {
        for (let i = index; i < this.treeService.getAll().length; i++) {
          if (indent >= this.treeService.getAll()[i].indent) {
            return this.treeService.getAll()[i].indent;
          }
        }
        return 0;
    }
    
    private indentForPosition(i: number): number {
        if(this.outOfBound(i)) {
          return 0;
        } else {
          return this.treeService.getAll()[i].indent;
        }
        
    }
    
    private outOfBound(i: number): boolean {
        return i >= this.treeService.getAll().length;
    }
}
  