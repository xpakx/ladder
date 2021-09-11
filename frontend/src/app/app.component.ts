import { Component } from '@angular/core';
import { TreeService } from './service/tree.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ladder';
  displayAddProject: string = "none";
  projectFav: boolean = false;

  constructor(public tree : TreeService) { }


  openProjectModal() {
    this.displayAddProject = "block";
  }

  closeProjectModal() {
    this.displayAddProject = "none";
  }

  addProjectModal() {
    this.displayAddProject = "none";
  }

  switchProjectFav() {
    this.projectFav = !this.projectFav;
  }
}
