import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { DndDropEvent } from 'ngx-drag-drop';
import { Label } from 'src/app/entity/label';
import { LabelDetails } from 'src/app/entity/label-details';
import { AddLabelEvent } from 'src/app/entity/utils/add-label-event';
import { DeleteService } from 'src/app/service/delete-service.service';
import { LabelService } from 'src/app/service/label.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-label-list',
  templateUrl: './label-list.component.html',
  styleUrls: ['./label-list.component.css']
})
export class LabelListComponent implements OnInit, AfterViewInit {
  @Output() addLabelModal = new EventEmitter<boolean>();
  @Output() addLabelModalAbove = new EventEmitter<LabelDetails | undefined>();
  @Output() addLabelModalBelow = new EventEmitter<LabelDetails | undefined>();
  @Output() addLabelModalEdit = new EventEmitter<LabelDetails | undefined>();
  @Output() addLabel = new EventEmitter<AddLabelEvent>();

  displayLabelModal: boolean = false;

  constructor(public tree : TreeService, private router: Router,
    private renderer: Renderer2, private labelService: LabelService, 
    private deleteService: DeleteService) { }

  ngOnInit(): void {
  }

  openLabelModal() {
    this.addLabel.emit({label: undefined, after: false, before: false});
  }

  toLabel(id: number) {
    this.router.navigate(['/label/'+id]);
  }

  switchLabelCollapse() {
    this.tree.labelCollapsed = !this.tree.labelCollapsed;
  }

  showContextMenu: boolean = false;
  contextMenuJustOpened: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  contextMenuLabel: LabelDetails | undefined;
  @ViewChild('labelContext', {read: ElementRef}) contextMenuElem!: ElementRef;

  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextMenu &&
        !this.contextMenuElem.nativeElement.contains(e.target)){
          if(this.contextMenuJustOpened) {
            this.contextMenuJustOpened = false
          } else {
            this.showContextMenu = false;
          }
      }
    });
  }

  openContextMenu(event: MouseEvent, label: LabelDetails) {
	  this.contextMenuLabel = label;
    this.showContextMenu = true;
    this.contextMenuJustOpened = true;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
  }

  closeContextMenu() {
    this.contextMenuLabel = undefined;
    this.showContextMenu = false;
  }

  askForDelete() {
    if(this.contextMenuLabel) {
      this.deleteService.openModalForLabel(this.contextMenuLabel);
    }
    this.closeContextMenu();
  }

  openLabelModalWithLabel() {
    this.addLabel.emit({label: this.contextMenuLabel, after: false, before: false});
    this.closeContextMenu();
  }

  openProjectModalAbove() {
    this.addLabel.emit({label: this.contextMenuLabel, after: false, before: true});
    this.closeContextMenu();
  }

  openProjectModalBelow() {
    this.addLabel.emit({label: this.contextMenuLabel, after: true, before: false});
    this.closeContextMenu();
  }


  updateLabelFav() {
    if(this.contextMenuLabel) {
      this.labelService.updateLabelFav(this.contextMenuLabel.id, {flag: !this.contextMenuLabel.favorite}).subscribe(
        (response: Label) => {
        this.tree.changeLabelFav(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }

    this.closeContextMenu();
  }

  // drag'n'drop

  draggedId: number | undefined;

  onDragStart(id: number) {
	  this.draggedId = id;
  }

  onDragEnd() {
	  this.draggedId = undefined;
  }

  isDragged(id: number): boolean {
    return this.draggedId == id;
  }
  
  onDrop(event: DndDropEvent, target: LabelDetails) {
    let id = Number(event.data);
    
    this.labelService.moveLabelAfter({id: target.id}, id).subscribe(
        (response: Label, afterId: number = target.id) => {
        this.tree.moveLabelAfter(response, afterId);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  onDropFirst(event: DndDropEvent) {
    let id = Number(event.data);
    this.labelService.moveLabelToBeginning(id).subscribe(
        (response: Label) => {
        this.tree.moveLabelAsFirst(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }
}
