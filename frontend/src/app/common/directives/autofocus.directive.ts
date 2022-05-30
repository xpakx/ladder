import { Directive, ElementRef, AfterViewInit } from '@angular/core';

@Directive({
  selector: '[autofocus]'
})
export class AutofocusDirective implements AfterViewInit {

  constructor(private host: ElementRef) { }

  ngAfterViewInit(): void {
    this.host.nativeElement.focus();
  }
}
