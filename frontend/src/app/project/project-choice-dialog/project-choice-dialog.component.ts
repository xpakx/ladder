import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { TreeService } from 'src/app/utils/tree.service';

@Component({
  selector: 'app-project-choice-dialog',
  templateUrl: './project-choice-dialog.component.html',
  styleUrls: ['./project-choice-dialog.component.css']
})
export class ProjectChoiceDialogComponent implements OnInit {
  @Input() project: ProjectTreeElem | undefined;
  @Output() closeEvent = new EventEmitter<ProjectTreeElem | undefined>();
  @Output() cancelEvent = new EventEmitter<boolean>();
  projectSelectForm: UntypedFormGroup | undefined;
  projects: ProjectTreeElem[] = [];

  constructor(private fb: UntypedFormBuilder, public tree: TreeService) { }

  ngOnInit(): void {
    this.projectSelectForm = this.fb.group({text: ''});
    this.projectSelectForm.valueChanges.subscribe(data => {
      this.getProjects(data.text);
    });
    this.getProjects();
  }

  getProjects(text: string = "") {
    this.projects = this.tree.filterProjects(text);
  }

  closeSelectProjectMenu() {
    this.cancelEvent.emit(true);
  }

  chooseProject(project: ProjectTreeElem | undefined) {
    this.project = project;
    this.closeEvent.emit(this.project);
  }

  @HostListener("window:keydown.escape", ["$event"])
  handleKeyboardEscapeEvent() {
    this.closeSelectProjectMenu();
  }
}
