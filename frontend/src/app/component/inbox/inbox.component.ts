import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskTreeElem } from 'src/app/task/dto/task-tree-elem';
import { AddEvent } from 'src/app/entity/utils/add-event';
import { RedirectionService } from 'src/app/service/redirection.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-inbox',
  templateUrl: './inbox.component.html',
  styleUrls: ['./inbox.component.css']
})
export class InboxComponent implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  todayDate: Date | undefined;
  showAddTaskForm: boolean = false;
  taskData: AddEvent<TaskTreeElem> = new AddEvent<TaskTreeElem>();

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService, private redirService: RedirectionService) {
  }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.redirService.setAddress("inbox")
      this.router.navigate(["load"]);
    }
  }
}
