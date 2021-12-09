import { Component, Input, OnInit } from '@angular/core';
import { LabelDetails } from 'src/app/entity/label-details';

@Component({
  selector: 'app-label-search-list',
  templateUrl: './label-search-list.component.html',
  styleUrls: ['./label-search-list.component.css']
})
export class LabelSearchListComponent implements OnInit {

  @Input("labelList") labelList: LabelDetails[] = [];

  constructor() { }

  ngOnInit(): void {
  }

}
