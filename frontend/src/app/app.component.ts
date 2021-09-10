import { Component } from '@angular/core';
import { TreeService } from './service/tree.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ladder';

  constructor(public tree : TreeService) { }
}
