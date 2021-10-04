export interface AddEvent<T> {
    object: T | undefined;
    after: boolean;
    before: boolean;
}