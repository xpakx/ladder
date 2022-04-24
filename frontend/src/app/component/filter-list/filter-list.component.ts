import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Filter } from 'src/app/entity/filter';
import { FilterDetails } from 'src/app/entity/filter-details';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { FilterTreeService } from 'src/app/service/filter-tree.service';
import { FilterService } from 'src/app/service/filter.service';
import { TreeService } from 'src/app/service/tree.service';
import { DraggableComponent } from '../abstract/draggable-component';
import { Animations } from '../common/animations';
import { ContextMenuElem } from '../context-menu/context-menu-elem';
import { Codes, MenuElems } from './filter-list-context-codes';

@Component({
  selector: 'app-filter-list',
  templateUrl: './filter-list.component.html',
  styleUrls: ['./filter-list.component.css'],
  animations: [Animations.collapseTrigger]
})
export class FilterListComponent  extends DraggableComponent<FilterDetails, Filter, FilterService, FilterTreeService>
implements OnInit {
  @Output() addFilter = new EventEmitter<AddEvent<FilterDetails>>();
  @Output() navEvent = new EventEmitter<boolean>();

  displayFilterModal: boolean = false;

  contextMenu: ContextMenuElem[] = [];
  favElem: ContextMenuElem = {name: MenuElems.addToFavs.name, icon: MenuElems.addToFavs.icon, code: MenuElems.addToFavs.code};

  constructor(public tree : TreeService, private router: Router, private filterService: FilterService, 
  private deleteService: DeleteService, protected treeService: FilterTreeService) {
    super(treeService, filterService);
  }

  ngOnInit(): void {
    this.prepareContextMenu();
  }

  openFilterModal() {
    this.addFilter.emit(new AddEvent<FilterDetails>());
  }

  toFilter(searchString: string) {
    this.router.navigate(['/search'], { queryParams: {search: searchString}});
    this.navEvent.emit(true);
  }

  switchFilterCollapse() {
    this.tree.filterCollapsed = !this.tree.filterCollapsed;
  }

  showContextMenu: boolean = false;
  contextMenuJustOpened: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  contextMenuFilter: FilterDetails | undefined;

  prepareContextMenu() {
    this.contextMenu.push(MenuElems.addFilterAbove);
    this.contextMenu.push(MenuElems.addFilterBelow);
    this.contextMenu.push(MenuElems.editFilter);
    this.contextMenu.push(this.favElem);
    this.contextMenu.push(MenuElems.deleteFilter);
  }

  closeContextMenu(code: number) {
    if(!this.contextMenuFilter) {return}
    let filter = this.contextMenuFilter;
    this.closeContextFilterMenu();
    
    switch(code) {
      case(Codes.addFilterAbove): { this.openFilterModalAbove(filter); break }
      case(Codes.addFilterBelow): { this.openFilterModalBelow(filter); break }
      case(Codes.editFilter): { this.openFilterModalWithFilter(filter); break }
      case(Codes.addToFavs): { this.updateFilterFav(filter); break }
      case(Codes.deleteFilter): { this.askForDelete(filter); break }
    }
  }

  openContextMenu(event: MouseEvent, filter: FilterDetails) {
    this.contextMenuFilter = filter;
    this.showContextMenu = true;
    if(this.contextMenuFilter?.favorite) {
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

  closeContextFilterMenu() {
    this.contextMenuFilter = undefined;
    this.showContextMenu = false;
  }

  askForDelete(filter: FilterDetails) {
    this.deleteService.openModalForFilter(filter);
  }

  openFilterModalWithFilter(filter: FilterDetails) {
    this.addFilter.emit(new AddEvent<FilterDetails>(filter));
  }

  openFilterModalAbove(filter: FilterDetails) {
    this.addFilter.emit(new AddEvent<FilterDetails>(filter, false, true));
  }

  openFilterModalBelow(filter: FilterDetails) {
    this.addFilter.emit(new AddEvent<FilterDetails>(filter, true, false));
  }


  updateFilterFav(filter: FilterDetails) {
    this.filterService.updateFilterFav(filter.id, {flag: !filter.favorite}).subscribe(
        (response: Filter) => {
        this.treeService.changeFilterFav(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

}
