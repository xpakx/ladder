import { HttpErrorResponse } from "@angular/common/http";
import { DndDropEvent } from "ngx-drag-drop";
import { ParentWithId } from "src/app/common/dto/parent-with-id";
import { MultilevelMovableService } from "src/app/common/multilevel-movable-service";
import { MultilevelMovableTreeService } from "src/app/common/multilevel-movable-tree-service";
import { IndentableTreeElem } from "./dto/indentable-tree-elem";

export class MultilevelDraggableComponent<P extends ParentWithId, R extends IndentableTreeElem<P>, T, S extends MultilevelMovableService<T>, U extends MultilevelMovableTreeService<T, R>>  {
    draggedId: number | undefined;
    
    constructor(protected treeService : U, protected service: S) { }
  
    onDragStart(id: number): void { 
        this.draggedId = id;
    }
  
    onDragEnd(): void {
        this.draggedId = undefined;
    }
  
    isDragged(id: number): boolean {
        return this.draggedId == id;
    }

    onDrop(event: DndDropEvent, target: IndentableTreeElem<P>, asChild: boolean = false): void { 
    let id = Number(event.data);
        if(!asChild)
        {
            this.service.moveAfter({id: target.id}, id).subscribe(
                (response: T, indent: number = target.indent, afterId: number = target.id) => {
                    this.treeService.moveAfter(response, afterId, indent);
                },
                (error: HttpErrorResponse) => {
                
                }
            );
        } else {
            this.service.moveAsChild({id: target.id}, id).subscribe(
                (response: T, indent: number = target.indent+1, afterId: number = target.id) => {
                    this.treeService.moveAsChild(response, afterId, indent);
                },
                (error: HttpErrorResponse) => {
                
                }
            );
        }
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

    hideDropZone(elem: R): boolean {
        return this.isDragged(elem.id) || 
        this.isParentDragged(elem.parentList) || 
        this.isParentCollapsed(elem.parentList);
    }


    collapseElem(elemId: number): void {
        let elem = this.treeService.getById(elemId);
        if(elem) {
          elem.collapsed = !elem.collapsed;
          this.service.updateCollapse(elem.id, {flag: elem.collapsed}).subscribe(
            (response: T, id: number = elemId) => {
                this.collapseInTree(response);
            },
            (error: HttpErrorResponse) => {
            
            }
          );
        }
    }

    collapseInTree(elem: T): void {
        
    } 

    isParentDragged(elems: IndentableTreeElem<P>[]): boolean {
        for(let elem of elems) {
        if(elem.id == this.draggedId) {
          return true;
        }
        }
        return false;
    }
      
    isElemCollapsed(elemId: number): boolean {
        let elem = this.treeService.getById(elemId);
        if(elem) {
          return elem.collapsed ? true : false;
        }
          return false;
    }
      
    isParentCollapsed(elems: IndentableTreeElem<P>[]): boolean {
        return elems.find((a) => a.collapsed) ? true : false;
    }

    getListForDropzones(i: number, elem: R): IndentableTreeElem<P>[] {
        let dropzones = elem.indent - this.amountOfDropzones(i, elem);
        return elem.parentList.slice(-dropzones);
    }
    
    protected amountOfDropzones(i: number, elem: R): number {
        return this.findFirstWithSmallerIndentAndReturnIndent(i + 1, elem.indent);
    }

    protected isNextElemDragged(i: number): boolean {
        return this.outOfBound(i+1) || this.isDragged(this.idForPosition(i+1))
    }
    
    getAmountOfNormalDropzones(i: number, elem: R): number {
        return this.amountOfDropzones(i, elem);
    }
    
    calculateAdditionalDropzones(i: number, elem: R): boolean {
        if(!elem.hasChildren) {
            if(!this.isNextElemDragged(i))
            {
                return elem.indent > this.indentForPosition(i+1)
            } else {
                return this.hasNextUndetachedElemSmallerIndent(i, elem);
            }
        } 
        if(!elem.collapsed) {
            return this.hasNextUndetachedElemSmallerIndent(i, elem) || this.onlyOneDraggedChild(i, elem);
        }
        return this.findFirstWithSmallerIndentAndReturnIndent(i+1, elem.indent) < elem.indent;
    }

    public onlyOneDraggedChild(i: number, elem: R): boolean {
        if(elem.hasChildren && this.indentForPosition(i+1) != this.indentForPosition(i+2) 
        && this.isDragged(this.idForPosition(i+1))) {
            return true;
        }
        return false;
    }

    private hasNextUndetachedElemSmallerIndent(i: number, elem: R): boolean {
        let nextUndetached = this.findFirstNonDetachedAndReturn(i + 1);
        return !nextUndetached || nextUndetached.indent < elem.indent;
    }

    findFirstNonDetachedAndReturn(index: number): R | undefined {
        for (let i = index; i < this.getElems().length; i++) {
            if (this.isNotDetachedFromProjectList(this.getElems()[i])) {
                return this.getElems()[i];
            }
        }
        return undefined;
    }
    
    private findFirstWithSmallerIndentAndReturnIndent(index: number, indent: number): number {
        for (let i = index; i < this.getElems().length; i++) {
          if (this.isCandidateForElemWithSmallerIndent(indent, this.getElems()[i])) {
            return this.getElems()[i].indent;
          }
        }
        return 0;
    }
    
    private isCandidateForElemWithSmallerIndent(indent: number, elem: R): boolean {
        return indent >= elem.indent && this.isNotDetachedFromProjectList(elem); 
    }

    private isNotDetachedFromProjectList(elem: R): boolean {
        return !this.isDragged(elem.id) && !this.isParentDragged(elem.parentList)
    }
    
    private indentForPosition(i: number): number {
        if(this.outOfBound(i)) {
          return 0;
        } else {
          return this.getElems()[i].indent;
        }
        
    }

    private idForPosition(i: number): number {
        return this.getElems()[i].id;       
    }
    
    private outOfBound(i: number): boolean {
        return i >= this.getElems().length;
    }

    protected getElems(): R[] {
        return this.treeService.getAll();
    }
}
  