import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, Output, Input, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Label } from 'src/app/entity/label';
import { LabelDetails } from 'src/app/entity/label-details';
import { LabelRequest } from 'src/app/entity/label-request';
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
  @Input() label: LabelDetails | undefined;
  @Input() after: boolean = false;
  @Input() before: boolean = false;
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

  addAfterLabelModal(request: LabelRequest, labelBefore: LabelDetails) {
    
  }
}
