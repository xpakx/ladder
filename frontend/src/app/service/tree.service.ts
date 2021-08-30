import { Injectable } from '@angular/core';
import { FullProjectTree } from '../entity/full-project-tree';

@Injectable({
  providedIn: 'root'
})
export class TreeService {
  public projects: FullProjectTree[] = [];

  constructor() { }

  load(tree: FullProjectTree[]) {
    this.projects = tree;
  }
}
