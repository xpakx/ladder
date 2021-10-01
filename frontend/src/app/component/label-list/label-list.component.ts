import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { LabelDetails } from 'src/app/entity/label-details';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-label-list',
  templateUrl: './label-list.component.html',
  styleUrls: ['./label-list.component.css']
})
export class LabelListComponent implements OnInit {
  @Output() addLabelModal = new EventEmitter<boolean>();
  @Output() addProjectModalAbove = new EventEmitter<LabelDetails | undefined>();
  @Output() addProjectModalBelow = new EventEmitter<LabelDetails | undefined>();
  @Output() addProjectModalEdit = new EventEmitter<LabelDetails | undefined>();

  displayLabelModal: boolean = false;

  constructor(public tree : TreeService, private router: Router) { }

  ngOnInit(): void {
  }

  openLabelModal() {
    this.addLabelModal.emit(true);
  }

  toLabel(id: number) {
    this.router.navigate(['/label/'+id]);
  }

  switchLabelCollapse() {
    this.tree.labelCollapsed = !this.tree.labelCollapsed;
  }
}
