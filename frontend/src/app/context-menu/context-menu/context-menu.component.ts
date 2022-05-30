import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, Renderer2, ViewChild } from '@angular/core';
import { ContextMenuElem } from './context-menu-elem';

@Component({
  selector: 'app-context-menu',
  templateUrl: './context-menu.component.html',
  styleUrls: ['./context-menu.component.css']
})
export class ContextMenuComponent implements OnInit, AfterViewInit {
  @Input() elems: ContextMenuElem[] = [];
  @Input() x: number = 0;
  @Input() y: number = 0;
  contextMenuX: number = 0;
  contextMenuY: number = 0;
  justOpened: boolean = true;

  @Output() actionEvent = new EventEmitter<number>();
  @Output() closeEvent = new EventEmitter<boolean>();

  @ViewChild('contextList', {read: ElementRef}) listElem!: ElementRef;

  constructor(private renderer: Renderer2) { }

  ngOnInit(): void {
    this.openContextMenu();
  }

  ngAfterViewInit(): void {
    this.renderer.listen('window', 'click',(e:Event)=>{
      if(!this.listElem.nativeElement.contains(e.target)) {
        if(this.justOpened) {
          this.justOpened = false;
        } else {
          this.closeEvent.emit(true);
        }
      }
    });
  }

  emit(code: number): void {
    this.actionEvent.emit(code);
  }

  openContextMenu(): void {
    if(this.x+250 > window.innerWidth) {
      this.contextMenuX = this.x-250;
    } else {
      this.contextMenuX = this.x;
    }
    
    let menuHeight: number = (this.elems.length)*24+20;
    if(this.y+menuHeight > window.innerHeight) {
      this.contextMenuY = this.y-menuHeight;
    } else {
      this.contextMenuY = this.y;
    }
  }
}
