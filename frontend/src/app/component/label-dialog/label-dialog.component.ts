import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, Output, Input, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Label } from 'src/app/entity/label';
import { LabelDetails } from 'src/app/entity/label-details';
import { LabelRequest } from 'src/app/entity/label-request';
import { AddLabelEvent } from 'src/app/entity/utils/add-label-event';
import { LabelService } from 'src/app/service/label.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-label-dialog',
  templateUrl: './label-dialog.component.html',
  styleUrls: ['./label-dialog.component.css']
})
export class LabelDialogComponent implements OnInit {
  addLabelForm: FormGroup;

  @Output() closeEvent = new EventEmitter<boolean>();
  @Input() data: AddLabelEvent | undefined;
  label: LabelDetails | undefined;
  after: boolean = false;
  before: boolean = false;
  editMode: boolean = false;

  favorite: boolean = false;

  constructor(private fb: FormBuilder, public tree : TreeService, 
    private labelService: LabelService) { 
    this.addLabelForm = this.fb.group({
      name: ['', Validators.required],
      color: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if(this.data) {
      this.label = this.data.label;
      this.after = this.data.after;
      this.before = this.data.before;
    }
    alert((this.label ? this.label.name : "No label") + " " + this.before + " " + this.after);
    if(this.label && !this.after && !this.before) {
      this.editMode = true;
    }
    if(this.editMode && this.label) {
      this.addLabelForm.setValue({
        name: this.label.name,
        color: this.label.color
      });
    }
  }

  closeLabelModal() {
    this.closeEvent.emit(true);
  }

  switchFav() {
    this.favorite = !this.favorite;
  }

  addLabelModal() {
    let request: LabelRequest = {
      name: this.addLabelForm.controls.name.value,
      color: this.addLabelForm.controls.color.value,
      favorite: this.favorite
    };
    
    this.closeLabelModal();
    
    if(this.label && this.after) {
      this.addAfterLabelModal(request, this.label);
    } else if(this.label && this.before) {
      this.addBeforeLabelModal(request, this.label);
    } else if(this.label) {
      this.editProjectModal(request, this.label.id);
    } else {
      this.addEndProjectModal(request)
    }
  }

  addEndProjectModal(request: LabelRequest) {
    this.labelService.addLabel(request).subscribe(
      (response: Label) => {
        this.tree.addNewLabel(response);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  addBeforeLabelModal(request: LabelRequest, labelBefore: LabelDetails) {
    this.labelService.addLabelBefore(request, labelBefore.id).subscribe(
      (response: Label, beforeId: number = labelBefore.id) => {
        this.tree.addNewLabelBefore(response, beforeId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  editProjectModal(request: LabelRequest, id: number) {
    this.labelService.updateLabel(id, request).subscribe(
      (response: Label) => {
        this.tree.updateLabel(response, id);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }

  addAfterLabelModal(request: LabelRequest, labelAfter: LabelDetails) {
    this.labelService.addLabelAfter(request, labelAfter.id).subscribe(
      (response: Label, afterId: number = labelAfter.id) => {
        this.tree.addNewLabelAfter(response, afterId);
      },
      (error: HttpErrorResponse) => {
       
      }
    );
  }
}
