import { Injectable } from '@angular/core';
import { Filter } from 'src/app/filter/dto/filter';
import { FilterDetails } from './dto/filter-details';
import { MovableTreeService } from 'src/app/common/movable-tree-service';

@Injectable({
  providedIn: 'root'
})
export class FilterTreeService implements MovableTreeService<Filter> {
  public list: FilterDetails[] = [];

  constructor() { }

  load(filters: FilterDetails[] = []) {
    this.list = filters;
    this.sort();
  }

  sort() {
    this.list.sort((a, b) => a.generalOrder - b.generalOrder);
  }

  addNewFilter(request: Filter) {
    this.list.push({
      name: request.name,
      id: request.id,
      color: request.color,
      searchString: request.searchString,
      favorite: request.favorite,
      generalOrder: request.generalOrder,
      modifiedAt: new Date(request.modifiedAt)
    });
    this.sort();
  }

  updateFilter(request: Filter, filterId: number) {
    let filter: FilterDetails | undefined = this.getById(filterId);
    if(filter) {
      filter.name = request.name;
      filter.color = request.color;
      filter.searchString = request.searchString;
      filter.favorite = request.favorite;
      filter.modifiedAt = new Date(request.modifiedAt);
    } 
  }

  getById(id: number): FilterDetails | undefined {
    return this.list.find((a) => a.id == id);
  }

  deleteFilter(filterId: number) {
    this.list = this.list.filter((a) => a.id != filterId);
  }

  changeFilterFav(response: Filter) {
    let filter = this.getById(response.id);
    if(filter) {
      filter.favorite = response.favorite;
      filter.modifiedAt = new Date(response.modifiedAt);
    }
  }

  addNewFilterBefore(filter: Filter, beforeId: number) {
    let beforeFilter = this.getById(beforeId);
    if(beforeFilter) {
      let fltr : FilterDetails = beforeFilter;
      filter.generalOrder = fltr.generalOrder;
      let filters = this.list
        .filter((a) => a.generalOrder >= fltr.generalOrder);
        for(let filt of filters) {
          filt.generalOrder = filt.generalOrder + 1;
        }
      this.addNewFilter(filter);
    }
  }

  addNewFilterAfter(filter: Filter, afterId: number) {
    let afterFilter = this.getById(afterId);
    if(afterFilter) {
      let fltr : FilterDetails = afterFilter;
      filter.generalOrder = fltr.generalOrder + 1;
      let filters = this.list
        .filter((a) => a.generalOrder > fltr.generalOrder);
        for(let filt of filters) {
          filt.generalOrder = filt.generalOrder + 1;
        }
      this.addNewFilter(filter);
    }
  }

  moveAfter(filter: Filter, afterId: number) {
    let afterFilter = this.getById(afterId);
    let movedFilter = this.getById(filter.id);
    if(afterFilter && movedFilter) {
      let fltr : FilterDetails = afterFilter;
      let filters = this.list
        .filter((a) => a.generalOrder > fltr.generalOrder);
        for(let filt of filters) {
          filt.generalOrder = filt.generalOrder + 1;
        }
      
      movedFilter.generalOrder = afterFilter.generalOrder+1;
      movedFilter.modifiedAt = new Date(filter.modifiedAt);

      this.sort();
    }
  }

  moveAsFirst(filter: Filter) {
    let movedFilter = this.getById(filter.id);
    if(movedFilter) {
      for(let fltr of this.list) {
        fltr.generalOrder = fltr.generalOrder + 1;
      }
      movedFilter.generalOrder = 1;
      movedFilter.modifiedAt = new Date(filter.modifiedAt);
      this.sort();
    }
  }

  sync(filters: FilterDetails[]) {
    for(let filter of filters) {
      let filterWithId = this.getById(filter.id);
      if(filterWithId) {
        filterWithId.color = filter.color;
        filterWithId.favorite = filter.favorite;
        filterWithId.generalOrder = filter.generalOrder;
        filterWithId.name = filter.name;
        filterWithId.searchString = filter.searchString;
        filterWithId.modifiedAt = new Date(filter.modifiedAt);
      } else {
        this.list.push(filter);
      }
    }
    this.sort();
  }
}
