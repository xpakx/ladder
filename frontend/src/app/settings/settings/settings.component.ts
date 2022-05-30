import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  view: number = 0;

  constructor() { }

  ngOnInit(): void {
  }

  chooseOption(num: number) {
    this.view = num;
  }

}
