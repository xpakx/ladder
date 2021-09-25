import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectTreeElem } from 'src/app/entity/project-tree-elem';
import { Task } from 'src/app/entity/task';
import { TaskDetails } from 'src/app/entity/task-details';
import { TaskTreeElem } from 'src/app/entity/task-tree-elem';
import { ProjectService } from 'src/app/service/project.service';
import { TaskService } from 'src/app/service/task.service';
import { TreeService } from 'src/app/service/tree.service';

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.css']
})
export class ProjectComponent implements OnInit {
  public invalid: boolean = false;
  public message: string = '';
  tasks: TaskTreeElem[] = [];
  todayDate: Date | undefined;
  project: ProjectTreeElem | undefined;
  showAddTaskForm: boolean = false;
  showEditTaskFormById: number | undefined;

  constructor(private router: Router, private route: ActivatedRoute, 
    private tree: TreeService, private taskService: TaskService) { }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.router.navigate(["load"]);
    }

    this.route.params.subscribe(routeParams => {
      this.loadProject(routeParams.id);
    });    
  }

  loadProject(id: number) {
    this.project = this.tree.getProjectById(id);
    this.tasks = this.tree.getTasksByProject(id);
  }

  openAddTaskForm() {
    this.closeEditTaskForm();
    this.showAddTaskForm = true;
  }

  closeAddTaskForm() {
    this.showAddTaskForm = false;
  }

  openEditTaskForm(id: number) {
    this.closeAddTaskForm();
    this.showEditTaskFormById = id;
  }

  closeEditTaskForm() {
    this.showEditTaskFormById = undefined;
  }

  completeTask(id: number) {
    let task = this.tree.getTaskById(id);
    if(task) {
    this.taskService.completeTask(id, {flag: !task.completed}).subscribe(
        (response: Task) => {
        this.tree.changeTaskCompletion(response);
      },
      (error: HttpErrorResponse) => {
      
      }
    );
    }
  }
}
