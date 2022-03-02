import { MultilevelMovableTreeService } from "./multilevel-movable-tree-service";
import { ProjectTreeElem } from '../entity/project-tree-elem';

export interface MovableTaskTreeService<T, R> extends MultilevelMovableTreeService<T, R> {
    moveTaskAsFirst(request: T, project: ProjectTreeElem | undefined): void
    collapse(response: T): void
}