import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, DoCheck, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CollabProjectData } from 'src/app/project/dto/collab-project-data';
import { Collaboration } from 'src/app/collaboration/dto/collaboration';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { CollabProjectTreeService } from 'src/app/project/collab-project-tree.service';
import { CollaborationService } from 'src/app/collaboration/collaboration.service';
import { RedirectionService } from 'src/app/utils/redirection.service';
import { TreeService } from 'src/app/utils/tree.service';

@Component({
  selector: 'app-collab-project',
  templateUrl: './collab-project.component.html',
  styleUrls: ['./collab-project.component.css']
})
export class CollabProjectComponent implements OnInit, AfterViewInit, DoCheck {
  public invalid: boolean = false;
  public message: string = '';
  project: CollabProjectData | undefined;
  collabAsProject: ProjectTreeElem | undefined;
  id!: number;

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService,  private redirService: RedirectionService, 
    private renderer: Renderer2,
    private projectTree: CollabProjectTreeService, private collabService: CollaborationService) {  }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.redirService.setAddress("collab/"+this.route.snapshot.params.id)
      this.router.navigate(["load"]);
    }

    this.route.params.subscribe(routeParams => {
      this.loadProject(routeParams.id);
    });    
  }

  ngDoCheck(): void {
    if(this.id) {
      if(!this.tree.getCollabProjectById(this.id)) {
        this.router.navigate(["/"]);
      }
    }
  }

  loadProject(id: number): void {
    this.id = id;
    this.project = this.projectTree.getCollabByProjectId(id);
    this.collabAsProject = this.project ? {
      id: this.project.project.id,
      name: this.project.project.name,
      parent: null,
      color: this.project.project.color,
      order: 0,
      realOrder: 0,
      hasChildren: false,
      indent: 0,
      parentList: [],
      favorite: false,
      collaborative: true,
      collapsed: false,
      modifiedAt: new Date()
    } : undefined;
  }

  showContextMenu: boolean = false;
  contextMenuJustOpened: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  @ViewChild('taskContext', {read: ElementRef}) taskContextMenuElem!: ElementRef;


  ngAfterViewInit(): void {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextMenu && 
        !this.taskContextMenuElem.nativeElement.contains(e.target)){
        if(this.contextMenuJustOpened) {
          this.contextMenuJustOpened = false
        } else {
          this.showContextMenu = false;
        }
      }
    })
  }

  openContextMenu(event: MouseEvent): void {
    this.showContextMenu = true;
    this.contextMenuJustOpened = true;
    this.contextMenuX = event.clientX-250;
    this.contextMenuY = event.clientY;
  }

  closeContextTaskMenu(): void {
    this.showContextMenu = false;
  }

  unsubscribe(): void {
    if(this.id) {
      this.collabService.unsubscribe(this.id, {flag: false}).subscribe(
        (collabs: Collaboration[]) => {
          this.tree.deleteCollabProject(this.id);
        },
        (error: HttpErrorResponse) => {}
      );
    }
  }
}
