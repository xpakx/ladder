import { Injectable } from '@angular/core';
import { Label } from 'src/app/label/dto/label';
import { LabelDetails } from './dto/label-details';
import { MovableTreeService } from 'src/app/common/movable-tree-service';

@Injectable({
  providedIn: 'root'
})
export class LabelTreeService implements MovableTreeService<Label> {
  public labels: LabelDetails[] = [];

  constructor() { }

  load(labels: LabelDetails[] = []) {
    this.labels = labels;
    this.sort();
  }

  sort() {
    this.labels.sort((a, b) => a.generalOrder - b.generalOrder);
  }

  addNewLabel(request: Label) {
    this.labels.push({
      name: request.name,
      id: request.id,
      color: request.color,
      favorite: request.favorite,
      generalOrder: request.generalOrder,
      modifiedAt: new Date(request.modifiedAt)
    });
    this.sort();
  }

  updateLabel(request: Label, labelId: number) {
    let label: LabelDetails | undefined = this.getLabelById(labelId);
    if(label) {
      label.name = request.name;
      label.color = request.color;
      label.favorite = request.favorite;
      label.modifiedAt = new Date(request.modifiedAt);
    } 
  }

  getLabelById(id: number): LabelDetails | undefined {
    return this.labels.find((a) => a.id == id);
  }

  filterLabels(text: string): LabelDetails[] {
    return this.labels.filter((a) => 
      a.name.toLowerCase().includes(text.toLowerCase())
    );
  }

  deleteLabel(labelId: number) {
    this.labels = this.labels.filter((a) => a.id != labelId);
  }

  changeLabelFav(response: Label) {
    let label = this.getLabelById(response.id);
    if(label) {
      label.favorite = response.favorite;
      label.modifiedAt = new Date(response.modifiedAt);
    }
  }

  addNewLabelBefore(label: Label, beforeId: number) {
    let beforeLabel = this.getLabelById(beforeId);
    if(beforeLabel) {
      let lbl : LabelDetails = beforeLabel;
      label.generalOrder = lbl.generalOrder;
      let labels = this.labels
        .filter((a) => a.generalOrder >= lbl.generalOrder);
        for(let lab of labels) {
          lab.generalOrder = lab.generalOrder + 1;
        }
      this.addNewLabel(label);
    }
  }

  addNewLabelAfter(label: Label, afterId: number) {
    let afterLabel = this.getLabelById(afterId);
    if(afterLabel) {
      let lbl : LabelDetails = afterLabel;
      label.generalOrder = lbl.generalOrder + 1;
      let labels = this.labels
        .filter((a) => a.generalOrder > lbl.generalOrder);
        for(let lab of labels) {
          lab.generalOrder = lab.generalOrder + 1;
        }
      this.addNewLabel(label);
    }
  }

  moveAfter(label: Label, afterId: number) {
    let afterLabel = this.getLabelById(afterId);
    let movedLabel = this.getLabelById(label.id);
    if(afterLabel && movedLabel) {
      let lbl : LabelDetails = afterLabel;
      let labels = this.labels
        .filter((a) => a.generalOrder > lbl.generalOrder);
        for(let lab of labels) {
          lab.generalOrder = lab.generalOrder + 1;
        }
      
      movedLabel.generalOrder = afterLabel.generalOrder+1;
      movedLabel.modifiedAt = new Date(label.modifiedAt);

      this.sort();
    }
  }

  moveAsFirst(label: Label) {
    let movedLabel = this.getLabelById(label.id);
    if(movedLabel) {
      for(let lbl of this.labels) {
        lbl.generalOrder = lbl.generalOrder + 1;
      }
      movedLabel.generalOrder = 1;
      movedLabel.modifiedAt = new Date(label.modifiedAt);
      this.sort();
    }
  }

  sync(labels: LabelDetails[]) {
    for(let label of labels) {
      let labelWithId = this.getLabelById(label.id);
      if(labelWithId) {
        labelWithId.color = label.color;
        labelWithId.favorite = label.favorite;
        labelWithId.generalOrder = label.generalOrder;
        labelWithId.name = label.name;
        labelWithId.modifiedAt = new Date(label.modifiedAt);
      } else {
        this.labels.push(label);
      }
    }
    this.sort();
  }
}
