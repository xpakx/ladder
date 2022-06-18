import { Component, EventEmitter, OnInit, Output, Renderer2 } from '@angular/core';
import { Router } from '@angular/router';
import { CollabProjectDetails } from 'src/app/project/dto/collab-project-details';
import { AddEvent } from 'src/app/common/utils/add-event';
import { CollabProjectTreeService } from 'src/app/project/collab-project-tree.service';
import { LabelService } from 'src/app/label/label.service';
import { TreeService } from 'src/app/utils/tree.service';
import { Animations } from 'src/app/common/animations';
import { ContextMenuElem } from 'src/app/context-menu/context-menu/context-menu-elem';
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
  @Output("hideScroll") hideScrollEvent = new EventEmitter<boolean>();

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
    this.hideScrollEvent.emit(true);
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
    this.hideScrollEvent.emit(false);
  }

  switchCollapse(): void {
    this.tree.collapsed = !this.tree.collapsed;
  }

  toProject(id: number): void {
    this.router.navigate(['/collab/'+id]);
    this.navEvent.emit(true);
  }
}
