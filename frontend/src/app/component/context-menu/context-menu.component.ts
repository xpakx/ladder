import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ContextMenuElem } from './context-menu-elem';

@Component({
  selector: 'app-context-menu',
  templateUrl: './context-menu.component.html',
  styleUrls: ['./context-menu.component.css']
})
export class ContextMenuComponent implements OnInit {
  @Input() elems: ContextMenuElem[] = [];
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  justOpened: boolean = false;

  @Output() actionEvent = new EventEmitter<number>();
  @Output() closeEvent = new EventEmitter<boolean>();

  constructor() { }

  ngOnInit(): void {
  }

  emit(code: number) {
    this.actionEvent.emit(code);
  }

  openContextMenu(event: MouseEvent) {
    this.justOpened = true;
    if(event.clientY+250 > window.innerWidth) {
      this.contextMenuX = event.clientX-250;
    } else {
      this.contextMenuX = event.clientX;
    }
    
    let menuHeight: number = (this.elems.length)*24+20;
    if(event.clientY+menuHeight > window.innerHeight) {
      this.contextMenuY = event.clientY-menuHeight;
    } else {
      this.contextMenuY = event.clientY;
    }
  }

}
