import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ExportService } from 'src/app/settings/export.service';

@Component({
  selector: 'app-settings-export',
  templateUrl: './settings-export.component.html',
  styleUrls: ['./settings-export.component.css']
})
export class SettingsExportComponent implements OnInit {

  constructor(private exportService: ExportService) { }

  ngOnInit(): void {
  }

  exportProjectsAsCSV() {
    this.exportService.getProjectsAsCSV().subscribe(
      (response: Blob) => {
        var csv = new Blob([response], { type: "text/csv" });
        var url= window.URL.createObjectURL(csv);
        window.open(url);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  exportTasksAsCSV() {
    this.exportService.getTasksAsCSV().subscribe(
      (response: Blob) => {
        var csv = new Blob([response], { type: "text/csv" });
        var url= window.URL.createObjectURL(csv);
        window.open(url);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  exportProjectsAsTXT() {
    this.exportService.getProjectsAsTXT().subscribe(
      (response: Blob) => {
        var txt = new Blob([response], { type: "text/txt" });
        var url= window.URL.createObjectURL(txt);
        window.open(url);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  exportTasksAsTXT() {
    this.exportService.getTasksAsTXT().subscribe(
      (response: Blob) => {
        var txt = new Blob([response], { type: "text/txt" });
        var url= window.URL.createObjectURL(txt);
        window.open(url);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

}
