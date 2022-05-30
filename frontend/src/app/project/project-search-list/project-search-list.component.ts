import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Project } from 'src/app/project/dto/project';
import { ProjectData } from 'src/app/project/dto/project-data';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { DeleteService } from 'src/app/utils/delete.service';
import { ProjectService } from 'src/app/project/project.service';
import { TreeService } from 'src/app/utils/tree.service';

@Component({
  selector: 'app-project-search-list',
  templateUrl: './project-search-list.component.html',
  styleUrls: ['./project-search-list.component.css']
})
export class ProjectSearchListComponent implements OnInit {
  @Input("projectList") projectList: ProjectTreeElem[] = [];
  @Input("archived") archived: boolean = false;

  constructor(private renderer: Renderer2, private projectService: ProjectService, private tree: TreeService, 
    private deleteService: DeleteService, private router: Router) { }

  ngOnInit(): void {
  }

  contextProjectMenu: ProjectTreeElem | undefined;
  showContextTaskMenu: boolean = false;
  contextTaskMenuJustOpened: boolean = false;
  taskContextMenuX: number = 0;
  taskContextMenuY: number = 0;
  @ViewChild('taskContext', {read: ElementRef}) taskContextMenuElem!: ElementRef;


  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextTaskMenu && 
        !this.taskContextMenuElem.nativeElement.contains(e.target)){
        if(this.contextTaskMenuJustOpened) {
          this.contextTaskMenuJustOpened = false
        } else {
          this.showContextTaskMenu = false;
        }
      }
    })
  }

  openContextTaskMenu(event: MouseEvent, project: ProjectTreeElem) {
	  this.contextProjectMenu = project;
    this.showContextTaskMenu = true;
    this.contextTaskMenuJustOpened = true;
    this.taskContextMenuX = event.clientX-250;
    this.taskContextMenuY = event.clientY;
  }

  closeContextTaskMenu() {
    this.contextProjectMenu = undefined;
    this.showContextTaskMenu = false;
  }

  restoreFromArchive() {
    if(this.contextProjectMenu) {
      this.projectService.archiveProject(this.contextProjectMenu.id, {flag: false}).subscribe(
        (response: Project) => {
          this.getRestoredData(response.id);
        },
        (error: HttpErrorResponse) => {
        
        }
      );
    }
  }

  private getRestoredData(id: number) {
    this.projectService.getProjectData(id).subscribe(
      (response: ProjectData) => {
        this.tree.syncProject(response);

      },
      (error: HttpErrorResponse) => {
      }
    );
  }

  askForDelete() {
    if(this.contextProjectMenu) {
      if(this.archived) {
        this.deleteService.openModalForArchivedProject(this.contextProjectMenu, this);
      } else {
        this.deleteService.openModalForProject(this.contextProjectMenu);
      }
    }
    this.closeContextTaskMenu();
  }

  deleteProjectFromArchive(id: number) {
    this.projectList = this.projectList.filter((a) => a.id != id)
  }

  toProject(id: number) {
    if(!this.archived) {
      this.router.navigate(['/project/'+id]);
    } else {
      this.router.navigate(['archive/project/'+id]);
    }
  }
}
