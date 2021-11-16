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
  priority: number | undefined;

  constructor(public tree: TreeService, private route: ActivatedRoute) { 
    this.route.queryParams.subscribe(params => {
      this.prepareSearch(params['search']);
    });
  }
  
  get tasks(): TaskTreeElem[] {
    return this.tree.getTasks()
      .filter((t) => t.title.includes(this.search))
      .filter((t) => !this.priority || t.priority == this.priority);
  }

  private prepareSearch(searchString: string) {
    console.log(searchString)
    const priorityRegex = new RegExp(/(^|\s)p[0-3]/g);
    const matches = searchString.match(priorityRegex);
    if(matches) {
      this.priority = Number(matches[0].substr(matches[0].length-1));
      for(let match of matches) {
        searchString = searchString.replace(match, '');
      }
    }
    this.search = searchString;
  }

  ngOnInit(): void {
  }

}
