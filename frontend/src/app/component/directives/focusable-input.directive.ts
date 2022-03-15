import { Directive, HostListener } from '@angular/core';
import { KeyboardManagerService } from 'src/app/service/keyboard-manager.service';

@Directive({
  selector: 'input, textarea'
})
export class FocusableInputDirective {

  constructor(private service: KeyboardManagerService) { }

  @HostListener('focus', ['$event'])
  onFocus() {
    this.service.blockLetters();
  }

  @HostListener('blur', ['$event'])
  onBlur() {
    this.service.unblockLetters();
  }
}
