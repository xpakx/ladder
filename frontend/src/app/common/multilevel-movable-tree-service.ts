import { MovableTreeService } from "../common/movable-tree-service";

export interface MultilevelMovableTreeService<T, R> extends MovableTreeService<T> {
    moveAfter(request: T, afterId: number, indent?: number): void
    moveAsChild(request: T, afterId: number, indent?: number): void
    moveAsFirst(request: T): void
    getById(id: number): R | undefined;
    getAll(): R[];
}