export interface MovableTreeService<T> {
    moveAfter(request: T, afterId: number): void
    moveAsFirst(request: T): void
}