import { MultilevelMovableTreeService } from "../common/multilevel-movable-tree-service";
import { ProjectTreeElem } from 'src/app/project/dto/project-tree-elem';

export interface MovableTaskTreeService<T, R> extends MultilevelMovableTreeService<T, R> {
    moveTaskAsFirst(request: T, project: ProjectTreeElem | undefined): void
    collapse(response: T): void
}