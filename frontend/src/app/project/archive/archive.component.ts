import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectDetails } from 'src/app/project/dto/project-details';
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';
import { ProjectService } from 'src/app/project/project.service';
import { RedirectionService } from 'src/app/utils/redirection.service';
import { TreeService } from 'src/app/utils/tree.service';

@Component({
  selector: 'app-archive',
  templateUrl: './archive.component.html',
  styleUrls: ['./archive.component.css']
})
export class ArchiveComponent implements OnInit {
  projects: ProjectTreeElem[] = [];

  constructor(private projectService: ProjectService, private redirService: RedirectionService, 
    private tree: TreeService, private  router: Router, private route: ActivatedRoute) { }

  ngOnInit(): void {
    if(!this.tree.isLoaded()) {
      this.redirService.setAddress("archive/")
      this.router.navigate(["load"]);
    }

    this.projectService.getArchivedProjects().subscribe(
      (response: ProjectDetails[]) => {
        this.projects = this.transformResponse(response);
    },
    (error: HttpErrorResponse) => {
    
    });
  }

  private transformResponse(projects: ProjectDetails[]): ProjectTreeElem[] {
    return projects.map((a) => this.transform(a));
  }

  private transform(project: ProjectDetails): ProjectTreeElem {
    return {
      id: project.id,
      name: project.name,
      parent: project.parent,
      color: project.color,
      order: project.generalOrder,
      realOrder: project.generalOrder,
      hasChildren: false,
      indent: 0,
      parentList: [],
      favorite: project.favorite,
      collapsed: project.collapsed,
      modifiedAt: new Date(project.modifiedAt),
      collaborative: project.collaborative
    }
  }

}

