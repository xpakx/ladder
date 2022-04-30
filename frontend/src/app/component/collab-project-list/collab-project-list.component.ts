import { Component, ElementRef, EventEmitter, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { CollabProjectDetails } from 'src/app/entity/collab-project-details';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { CollabProjectTreeService } from 'src/app/service/collab-project-tree.service';
import { LabelService } from 'src/app/service/label.service';
import { TreeService } from 'src/app/service/tree.service';
import { Animations } from '../common/animations';
import { ContextMenuElem } from '../context-menu/context-menu-elem';
import { MenuElems, Codes } from './collab-project-list-context-codes';

@Component({
  selector: 'app-collab-project-list',
  templateUrl: './collab-project-list.component.html',
  styleUrls: ['./collab-project-list.component.css'],
  animations: [Animations.collapseTrigger]
})
export class CollabProjectListComponent implements OnInit  {
  @Output() addLabel = new EventEmitter<AddEvent<CollabProjectDetails>>();
  @Output() navEvent = new EventEmitter<boolean>();
  displayProjectModal: boolean = false;
  contextMenu: ContextMenuElem[] = [];

  constructor(private router: Router,
    private renderer: Renderer2, private labelService: LabelService, 
    public tree: CollabProjectTreeService, public treeService: TreeService) { }

  ngOnInit(): void {
    this.prepareContextMenu();
  }

  showContextMenu: boolean = false;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  contextMenuProject: CollabProjectDetails | undefined;

  prepareContextMenu(): void {
    this.contextMenu.push(MenuElems.unknownAction);
  }

  openContextMenu(event: MouseEvent, project: CollabProjectDetails): void {
	  this.contextMenuProject = project;
    this.showContextMenu = true;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
  }

  closeContextMenu(code: number): void {
    if(!this.contextMenuProject) {return}
    let project = this.contextMenuProject;
    this.closeContextProjectMenu();
    
    switch(code) {
      case(Codes.unknownAction): { break }
    }
  }

  closeContextProjectMenu(): void {
    this.contextMenuProject = undefined;
    this.showContextMenu = false;
  }

  switchCollapse(): void {
    this.tree.collapsed = !this.tree.collapsed;
  }

  toProject(id: number): void {
    this.router.navigate(['/collab/'+id]);
    this.navEvent.emit(true);
  }
}
