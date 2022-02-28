import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { CollabProjectTreeService } from 'src/app/service/collab-project-tree.service';
import { RedirectionService } from 'src/app/service/redirection.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-collab-project',
  templateUrl: './collab-project.component.html',
  styleUrls: ['./collab-project.component.css']
})
export class CollabProjectComponent implements OnInit, AfterViewInit {
  public invalid: boolean = false;
  public message: string = '';
  project: ProjectTreeElem | undefined;
  id!: number;

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService,  private redirService: RedirectionService, 
    private renderer: Renderer2,
    private projectTree: CollabProjectTreeService) {  }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.redirService.setAddress("collab/"+this.route.snapshot.params.id)
      this.router.navigate(["load"]);
    }

    this.route.params.subscribe(routeParams => {
      this.loadProject(routeParams.id);
    });    
  }

  loadProject(id: number) {
    this.id = id;
    let collabProject = this.projectTree.getProjectById(id);
    if(collabProject) {
      this.project = {
        id: collabProject.id,
        name: collabProject.name,
        color: collabProject.color,
        parent: null,
        order: 0,
        realOrder: 0, 
        hasChildren: false,
        parentList: [], 
        indent: 0,
        favorite: collabProject.favorite,
        collapsed: false,
        modifiedAt: collabProject.modifiedAt
      }
    }
  }

  showContextMenu: boolean = false;
  contextMenuJustOpened: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  @ViewChild('taskContext', {read: ElementRef}) taskContextMenuElem!: ElementRef;


  ngAfterViewInit() {
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

  openContextMenu(event: MouseEvent) {
    this.showContextMenu = true;
    this.contextMenuJustOpened = true;
    this.contextMenuX = event.clientX-250;
    this.contextMenuY = event.clientY;
  }

  closeContextTaskMenu() {
    this.showContextMenu = false;
  }
}
