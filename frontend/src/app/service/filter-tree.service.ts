import { Injectable } from '@angular/core';
import { Filter } from '../entity/filter';
import { FilterDetails } from '../entity/filter-details';

@Injectable({
  providedIn: 'root'
})
export class FilterTreeService {
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

}
