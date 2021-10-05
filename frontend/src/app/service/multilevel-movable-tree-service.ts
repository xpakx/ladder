export interface MultilevelMovableTreeService<T, R> {
    moveAfter(request: T, indent: number, afterId: number): void
    moveAsChild(request: T, indent: number, afterId: number): void
    moveAsFirst(request: T): void
    getById(id: number): R | undefined;
    getAll(): R[];
}