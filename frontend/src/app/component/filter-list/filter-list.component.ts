import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Filter } from 'src/app/entity/filter';
import { FilterDetails } from 'src/app/entity/filter-details';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { DeleteService } from 'src/app/service/delete.service';
import { FilterTreeService } from 'src/app/service/filter-tree.service';
import { FilterService } from 'src/app/service/filter.service';
import { TreeService } from 'src/app/service/tree.service';
import { DraggableComponent } from '../abstract/draggable-component';

@Component({
  selector: 'app-filter-list',
  templateUrl: './filter-list.component.html',
  styleUrls: ['./filter-list.component.css']
})
export class FilterListComponent  extends DraggableComponent<FilterDetails, Filter, FilterService, FilterTreeService>
implements OnInit, AfterViewInit {
  @Output() addFilter = new EventEmitter<AddEvent<FilterDetails>>();

  displayFilterModal: boolean = false;

  constructor(public tree : TreeService, private router: Router,
  private renderer: Renderer2, private filterService: FilterService, 
  private deleteService: DeleteService, protected treeService: FilterTreeService) {
    super(treeService, filterService);
  }

  ngOnInit(): void {}

  openFilterModal() {
    this.addFilter.emit(new AddEvent<FilterDetails>());
  }

  toFilter(searchString: string) {
    this.router.navigate(['/search'], { queryParams: {search: searchString}});
  }

  switchFilterCollapse() {
    this.tree.filterCollapsed = !this.tree.filterCollapsed;
  }

  showContextMenu: boolean = false;
  contextMenuJustOpened: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  contextMenuFilter: FilterDetails | undefined;
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

  openContextMenu(event: MouseEvent, filter: FilterDetails) {
    this.contextMenuFilter = filter;
    this.showContextMenu = true;
    this.contextMenuJustOpened = true;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
  }

  closeContextMenu() {
    this.contextMenuFilter = undefined;
    this.showContextMenu = false;
  }

  askForDelete() {
    if(this.contextMenuFilter) {
      this.deleteService.openModalForFilter(this.contextMenuFilter);
    }
    this.closeContextMenu();
  }

  openFilterModalWithLabel() {
    this.addFilter.emit(new AddEvent<FilterDetails>(this.contextMenuFilter));
    this.closeContextMenu();
  }

  openFilterModalAbove() {
    this.addFilter.emit(new AddEvent<FilterDetails>(this.contextMenuFilter, false, true));
    this.closeContextMenu();
  }

  openFilterModalBelow() {
    this.addFilter.emit(new AddEvent<FilterDetails>(this.contextMenuFilter, true, false));
    this.closeContextMenu();
  }


  updateFilterFav() {
    if(this.contextMenuFilter) {
      this.filterService.updateFilterFav(this.contextMenuFilter.id, {flag: !this.contextMenuFilter.favorite}).subscribe(
        (response: Filter) => {
        this.treeService.changeFilterFav(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
    }

    this.closeContextMenu();
  }

}
