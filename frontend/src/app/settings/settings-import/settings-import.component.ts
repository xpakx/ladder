import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { ImportService } from 'src/app/settings/import.service';

@Component({
  selector: 'app-settings-import',
  templateUrl: './settings-import.component.html',
  styleUrls: ['./settings-import.component.css']
})
export class SettingsImportComponent implements OnInit {
  file?: File;
  project: ProjectTreeElem | undefined;
  showSelectProjectModal: boolean = false;
  @ViewChild('fileselection', {static: true}) fileselectionButton?: ElementRef;

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

  openFileSelection() {
    if(this.fileselectionButton) {
      this.fileselectionButton.nativeElement.click();
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

  importProjectTasksAsCSV() {
    if(!this.file || !this.project) {
      return;
    }
    this.importService.sendProjectTasksAsCSV(this.file, this.project.id).subscribe(
      (response: any) => {
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  importTasksAsCSV() {
    if(!this.file) {
      return;
    }
    this.importService.sendTasksAsCSV(this.file).subscribe(
      (response: any) => {
      },
      (error: HttpErrorResponse) => {
      
      }
    );
  }

  openProjectSelection() {
    this.showSelectProjectModal = true;
  }

  cancelProjectSelection() {
    this.showSelectProjectModal = false;
  }

  closeSelectProjectModal(project: ProjectTreeElem | undefined) {
    this.project = project;
    this.showSelectProjectModal = false;
  }
}
