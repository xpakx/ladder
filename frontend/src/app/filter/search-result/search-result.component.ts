import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HabitDetails } from 'src/app/habit/dto/habit-details';
import { LabelDetails } from 'src/app/label/dto/label-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { TreeService } from 'src/app/utils/tree.service';

@Component({
  selector: 'app-search-result',
  templateUrl: './search-result.component.html',
  styleUrls: ['./search-result.component.css']
})
export class SearchResultComponent implements OnInit {
  search: string = "";
  priority: number | undefined;
  searchLabels: LabelDetails[] = [];
  project: ProjectTreeElem | undefined;
  date: Date | undefined;

  constructor(public tree: TreeService, private route: ActivatedRoute,
    private router: Router) { 
    this.route.queryParams.subscribe(params => {
      this.prepareSearch(params['search']);
    });
  }
  
  get tasks(): TaskTreeElem[] {
    return this.tree.getTasks()
      .filter((t) => t.title.includes(this.search))
      .filter((t) => !this.priority || t.priority == this.priority)
      .filter((t) => this.searchLabels.length==0 || this.searchLabels.every((a) => t.labels.find((b) => b.id == a.id)))
      .filter((t) => !this.project || (t. project && t.project.id == this.project.id))
      .filter((t) => !this.date || (t.due && this.sameDay(t.due, this.date)));
  }

  get habits(): HabitDetails[] {
    return this.tree.getHabits()
      .filter((t) => t.title.includes(this.search))
      .filter((t) => !this.priority || t.priority == this.priority)
      .filter((t) => this.searchLabels.length==0 || this.searchLabels.every((a) => t.labels.find((b) => b.id == a.id)))
      .filter((t) => !this.project || (t. project && t.project.id == this.project.id));
  }

  get labels(): LabelDetails[] {
    if(this.priority || this.project || this.date || this.searchLabels.length > 0) {
      return [];
    }
    return this.tree.getLabels()
      .filter((t) => t.name.includes(this.search));
  }

  get projects(): ProjectTreeElem[] {
    if(this.priority || this.project || this.date || this.searchLabels.length > 0) {
      return [];
    }
    return this.tree.getProjects()
      .filter((t) => t.name.includes(this.search));
  }

  sameDay(date1: Date, date2: Date): boolean {
    return date1.getFullYear() == date2.getFullYear() && date1.getDate() == date2.getDate() && date1.getMonth() == date2.getMonth();
  }

  private prepareSearch(searchString: string) {
    console.log(searchString)

    this.priority = undefined;
    this.searchLabels = [];
    this.project = undefined;
    this.date = undefined;
    
    const priorityRegex = new RegExp(/(^|\s)p[0-3]/g);
    const priorityMatches = searchString.match(priorityRegex);
    if(priorityMatches) {
      this.priority = Number(priorityMatches[0].trim().substr(1));
      for(let match of priorityMatches) {
        searchString = searchString.replace(match, '');
      }
    }

    const labelRegex = new RegExp(/(^|\s)#[A-Za-z0-9]*/g);
    const labelMatches = searchString.match(labelRegex);
    if(labelMatches) {
      let labelNames = labelMatches
        .map((l) => l.trim())
        .map((l) => l.substr(1));
      this.searchLabels = this.tree.getLabels().filter((l) => 
        labelNames.filter((a) => l.name.includes(a)).length>0
      );
      for(let match of labelMatches) {
        searchString = searchString.replace(match, '');
      }
    }

    const projectRegex = new RegExp(/(^|\s)\+[A-Za-z0-9]*/g);
    const projectMatches = searchString.match(projectRegex);
    if(projectMatches) {
      let projectId = Number(projectMatches[0].trim().substr(1));
      this.project = this.tree.getProjectById(projectId);
      for(let match of projectMatches) {
        searchString = searchString.replace(match, '');
      }
    }

    const dateRegex = new RegExp(/(^|\s)(0?[1-9]|[12][0-9]|3[01])[\/\-](0?[1-9]|1[012])[\/\-]\d{4}$/g);
    const dateMatches = searchString.match(dateRegex);
    if(dateMatches) {
      let dateElem = dateMatches[0].includes('-') ? dateMatches[0].split('-') : dateMatches[0].split('/'); 
      this.date = new Date(Number(dateElem[2]), Number(dateElem[1])-1, Number(dateElem[0]));
      console.log(this.date)
      for(let match of dateMatches) {
        searchString = searchString.replace(match, '');
      }
    }

    console.log(this.searchLabels.map((a) => a.name))
    console.log(searchString)
    this.search = searchString.trim();
  }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }   
  }

}
