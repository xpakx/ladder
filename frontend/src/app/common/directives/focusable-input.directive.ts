import { Directive, HostListener } from '@angular/core';
import { KeyboardManagerService } from 'src/app/utils/keyboard-manager.service';

@Directive({
  selector: 'input, textarea'
})
export class FocusableInputDirective {

  constructor(private service: KeyboardManagerService) { }

  @HostListener('focus', ['$event'])
  onFocus(): void {
    this.service.blockLetters();
  }

  @HostListener('blur', ['$event'])
  onBlur(): void {
    this.service.unblockLetters();
  }

  ngOnDestroy(): void {
    this.service.unblockLetters();
  }
}
