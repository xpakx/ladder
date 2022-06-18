import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Label } from '../dto/label';
import { LabelDetails } from '../dto/label-details';
import { AddEvent } from 'src/app/common/utils/add-event';
import { DeleteService } from 'src/app/utils/delete.service';
import { LabelTreeService } from '../label-tree.service';
import { LabelService } from '../label.service';
import { TreeService } from 'src/app/utils/tree.service';
import { DraggableComponent } from '../../common/draggable-component';
import { Animations } from '../../common/animations';
import { ContextMenuElem } from '../../context-menu/context-menu/context-menu-elem';
import { Codes, MenuElems } from './label-list-context-codes';

@Component({
  selector: 'app-label-list',
  templateUrl: './label-list.component.html',
  styleUrls: ['./label-list.component.css'],
  animations: [Animations.collapseTrigger]
})
export class LabelListComponent extends DraggableComponent<LabelDetails, Label, LabelService, LabelTreeService>
    implements OnInit {
  @Output() addLabel = new EventEmitter<AddEvent<LabelDetails>>();
  @Output() navEvent = new EventEmitter<boolean>();
  @Output("hideScroll") hideScrollEvent = new EventEmitter<boolean>();

  displayLabelModal: boolean = false;

  contextMenu: ContextMenuElem[] = [];
  favElem: ContextMenuElem = {name: MenuElems.addToFavs.name, icon: MenuElems.addToFavs.icon, code: MenuElems.addToFavs.code};

  constructor(public tree : TreeService, private router: Router,
    private labelService: LabelService, 
    private deleteService: DeleteService, protected treeService: LabelTreeService) {
      super(treeService, labelService);
     }

  ngOnInit(): void {
    this.prepareContextMenu();
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

  prepareContextMenu() {
    this.contextMenu.push(MenuElems.addLabelAbove);
    this.contextMenu.push(MenuElems.addLabelBelow);
    this.contextMenu.push(MenuElems.editLabel);
    this.contextMenu.push(this.favElem);
    this.contextMenu.push(MenuElems.deleteLabel);
  }

  closeContextMenu(code: number) {
    if(!this.contextMenuLabel) {return}
    let label = this.contextMenuLabel;
    this.closeContextLabelMenu();
    
    switch(code) {
      case(Codes.addLabelAbove): { this.openLabelModalAbove(label); break }
      case(Codes.addLabelBelow): { this.openLabelModalBelow(label); break }
      case(Codes.editLabel): { this.openLabelModalWithLabel(label); break }
      case(Codes.addToFavs): { this.updateLabelFav(label); break }
      case(Codes.deleteLabel): { this.askForDelete(label); break }
    }
  }

  openContextMenu(event: MouseEvent, label: LabelDetails) {
    this.contextMenuLabel = label;
    this.showContextMenu = true;
    this.hideScrollEvent.emit(true);
    if(this.contextMenuLabel?.favorite) {
      this.favElem.name =  MenuElems.deleteFromFavs.name;
      this.favElem.icon =  MenuElems.deleteFromFavs.icon;
    } else {
      this.favElem.name =  MenuElems.addToFavs.name;
      this.favElem.icon =  MenuElems.addToFavs.icon;
    }
    this.contextMenuJustOpened = true;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
  }

  closeContextLabelMenu() {
    this.contextMenuLabel = undefined;
    this.showContextMenu = false;
    this.hideScrollEvent.emit(false);
  }

  askForDelete(label: LabelDetails) {
    this.deleteService.openModalForLabel(label);
  }

  openLabelModalWithLabel(label: LabelDetails) {
    this.addLabel.emit(new AddEvent<LabelDetails>(label));
  }

  openLabelModalAbove(label: LabelDetails) {
    this.addLabel.emit(new AddEvent<LabelDetails>(label, false, true));
  }

  openLabelModalBelow(label: LabelDetails) {
    this.addLabel.emit(new AddEvent<LabelDetails>(label, true, false));
  }

  updateLabelFav(label: LabelDetails) {
    this.labelService.updateLabelFav(label.id, {flag: !label.favorite}).subscribe(
        (response: Label) => {
        this.treeService.changeLabelFav(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }
}
