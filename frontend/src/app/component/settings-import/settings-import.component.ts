import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ImportService } from 'src/app/service/import.service';

@Component({
  selector: 'app-settings-import',
  templateUrl: './settings-import.component.html',
  styleUrls: ['./settings-import.component.css']
})
export class SettingsImportComponent implements OnInit {
  file?: File;

  constructor(private importService: ImportService) { }
  

  ngOnInit(): void {
  }

  selectFile(event: Event) {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if(fileList && fileList.length > 0) {
      let firstFile = fileList.item(0);
      this.file = firstFile ? firstFile : undefined;
    }
  }

  importProjectsAsCSV() {
    if(!this.file) {
      return;
    }
    this.importService.sendProjectsAsCSV(this.file).subscribe(
      (response: any) => {
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

}
