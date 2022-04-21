import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Label } from 'src/app/entity/label';
import { LabelDetails } from 'src/app/entity/label-details';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { LabelTreeService } from 'src/app/service/label-tree.service';
import { LabelService } from 'src/app/service/label.service';
import { TreeService } from 'src/app/service/tree.service';
import { DraggableComponent } from '../abstract/draggable-component';
import { Animations } from '../common/animations';

@Component({
  selector: 'app-label-list',
  templateUrl: './label-list.component.html',
  styleUrls: ['./label-list.component.css'],
  animations: [Animations.collapseTrigger]
})
export class LabelListComponent extends DraggableComponent<LabelDetails, Label, LabelService, LabelTreeService>
    implements OnInit, AfterViewInit 
   {
  @Output() addLabel = new EventEmitter<AddEvent<LabelDetails>>();
  @Output() navEvent = new EventEmitter<boolean>();

  displayLabelModal: boolean = false;

  constructor(public tree : TreeService, private router: Router,
    private renderer: Renderer2, private labelService: LabelService, 
    private deleteService: DeleteService, protected treeService: LabelTreeService) {
      super(treeService, labelService);
     }

  ngOnInit(): void {
  }

  openLabelModal() {
    this.addLabel.emit(new AddEvent<LabelDetails>());
  }

  toLabel(id: number) {
    this.router.navigate(['/label/'+id]);
    this.navEvent.emit(true);
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
    this.addLabel.emit(new AddEvent<LabelDetails>(this.contextMenuLabel));
    this.closeContextMenu();
  }

  openProjectModalAbove() {
    this.addLabel.emit(new AddEvent<LabelDetails>(this.contextMenuLabel, false, true));
    this.closeContextMenu();
  }

  openProjectModalBelow() {
    this.addLabel.emit(new AddEvent<LabelDetails>(this.contextMenuLabel, true, false));
    this.closeContextMenu();
  }


  updateLabelFav() {
    if(this.contextMenuLabel) {
      this.labelService.updateLabelFav(this.contextMenuLabel.id, {flag: !this.contextMenuLabel.favorite}).subscribe(
        (response: Label) => {
        this.treeService.changeLabelFav(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
    }

    this.closeContextMenu();
  }
}
