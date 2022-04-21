import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { animate, style, transition, trigger, group, query } from '@angular/animations';
import { CollabProjectDetails } from 'src/app/entity/collab-project-details';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { CollabProjectTreeService } from 'src/app/service/collab-project-tree.service';
import { LabelService } from 'src/app/service/label.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-collab-project-list',
  templateUrl: './collab-project-list.component.html',
  styleUrls: ['./collab-project-list.component.css'],
  animations: [
    trigger('collapse', [
      transition(':enter', [
        style({ height: '0' }),
        group([
          animate('.375s', style({ height: '*' })),
          query('.projects_list_container',[
            style({ transform: 'translateY(-100%)' }),
            animate('.375s', style({ transform: 'translateY(0)' }))
          ])
        ])
      ]),
      transition(':leave', [
        style({ overflow: 'hidden' }),
        group([
          animate('.375s', style({ height: '0' })),
          query('.projects_list_container',[
            style({ transform: 'translateY(0)' }),
            animate('.375s', style({ transform: 'translateY(-100%)' }))
          ])
        ])
      ]),
    ])
  ]
})
export class CollabProjectListComponent implements OnInit, AfterViewInit  {
  @Output() addLabel = new EventEmitter<AddEvent<CollabProjectDetails>>();
  @Output() navEvent = new EventEmitter<boolean>();
  displayProjectModal: boolean = false;

  constructor(private router: Router,
    private renderer: Renderer2, private labelService: LabelService, 
    public tree: CollabProjectTreeService, public treeService: TreeService) { }

  ngOnInit(): void {
  }

  showContextMenu: boolean = false;
  contextMenuJustOpened: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  contextMenuProject: CollabProjectDetails | undefined;
  @ViewChild('projectContext', {read: ElementRef}) contextMenuElem!: ElementRef;

  ngAfterViewInit() {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(this.showContextMenu &&
        !this.contextMenuElem.nativeElement.contains(e.target)){
          if(this.contextMenuJustOpened) {
            this.contextMenuJustOpened = false
          } else {
            this.showContextMenu = false;
          }
      }
    });
  }

  openContextMenu(event: MouseEvent, project: CollabProjectDetails) {
	  this.contextMenuProject = project;
    this.showContextMenu = true;
    this.contextMenuJustOpened = true;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
  }

  closeContextMenu() {
    this.contextMenuProject = undefined;
    this.showContextMenu = false;
  }

  switchCollapse() {
    this.tree.collapsed = !this.tree.collapsed;
  }

  toProject(id: number) {
    this.router.navigate(['/collab/'+id]);
    this.navEvent.emit(true);
  }
}
