import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { LabelDetails } from 'src/app/entity/label-details';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-label-choice-dialog',
  templateUrl: './label-choice-dialog.component.html',
  styleUrls: ['./label-choice-dialog.component.css']
})
export class LabelChoiceDialogComponent implements OnInit {
  @Input() labels: LabelDetails[] = [];
  @Output() closeEvent = new EventEmitter<LabelDetails[]>();
  @Output() cancelEvent = new EventEmitter<boolean>();
  labelSelectForm: FormGroup | undefined;
  visibleLabels: LabelDetails[] = [];

  constructor(private fb: FormBuilder, public tree: TreeService) { }

  ngOnInit(): void {
    this.labelSelectForm = this.fb.group({text: ''});
    this.labelSelectForm.valueChanges.subscribe(data => {
      this.getLabels(data.text);
    });
    this.getLabels();
  }

  getLabels(text: string = "") {
    this.visibleLabels = this.tree.filterLabels(text);
  }

  closeSelectLabelMenu() {
    this.cancelEvent.emit(true);
  }

  choice(label: LabelDetails) {
    if(this.isLabelChosen(label.id)) {
      this.cancelChoice(label);
    } else {
      this.chooseLabel(label);
    }
  }

  chooseLabel(label: LabelDetails) {
    this.labels.push(label);
  }

  cancelChoice(label: LabelDetails) {
    this.labels = this.labels.filter((a) => a.id != label.id);
  }

  chooseLabels() {
    this.closeEvent.emit(this.labels);
  }

  isLabelChosen(labelId: number): boolean {
    return this.labels.find((a) => a.id == labelId) !== undefined;
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent() {
    this.closeSelectLabelMenu();
  }

  @HostListener("window:keydown.enter", ["$event"])
  handleKeyboardEnterEvent() {
    this.chooseLabels();
  }
}
