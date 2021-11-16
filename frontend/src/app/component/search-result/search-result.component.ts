import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LabelDetails } from 'src/app/entity/label-details';
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
  labels: LabelDetails[] = [];

  constructor(public tree: TreeService, private route: ActivatedRoute) { 
    this.route.queryParams.subscribe(params => {
      this.prepareSearch(params['search']);
    });
  }
  
  get tasks(): TaskTreeElem[] {
    return this.tree.getTasks()
      .filter((t) => t.title.includes(this.search))
      .filter((t) => !this.priority || t.priority == this.priority)
      .filter((t) => this.labels.length==0 || this.labels.every((a) => t.labels.find((b) => b.id == a.id)))
  }

  private prepareSearch(searchString: string) {
    console.log(searchString)
    const priorityRegex = new RegExp(/(^|\s)p[0-3]/g);
    const priorityMatches = searchString.match(priorityRegex);
    if(priorityMatches) {
      this.priority = Number(priorityMatches[0].trim().substr(1));
      for(let match of priorityMatches) {
        searchString = searchString.replace(match, '');
      }
    }

    const labelRegex = new RegExp(/(^|\s)\+[A-Za-z0-9]*/g);
    const labelMatches = searchString.match(labelRegex);
    if(labelMatches) {
      let labelNames = labelMatches
        .map((l) => l.trim())
        .map((l) => l.substr(1));
      console.log(labelNames)
      this.labels = this.tree.getLabels().filter((l) => 
        labelNames.filter((a) => l.name.includes(a)).length>0
      );
      console.log(this.labels.map((a) => a.name))
      for(let match of labelMatches) {
        searchString = searchString.replace(match, '');
      }
    }

    this.search = searchString.trim();
  }

  ngOnInit(): void {
  }

}
