import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-search-result',
  templateUrl: './search-result.component.html',
  styleUrls: ['./search-result.component.css']
})
export class SearchResultComponent implements OnInit {
  search: string = "";

  constructor(public tree: TreeService, private route: ActivatedRoute) { 
    this.route.queryParams.subscribe(params => {
      this.prepareSearch(params['search']);
    });
  }
  
  get tasks(): TaskTreeElem[] {
    return this.tree.getTasks()
      .filter((t) => t.title.includes(this.search));
  }

  private prepareSearch(searchString: string) {
    this.search = searchString;
  }

  ngOnInit(): void {
  }

}
