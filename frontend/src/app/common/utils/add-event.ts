export class AddEvent<T> {

    constructor(public object?: T, public after: boolean = false, 
        public before: boolean = false, public asChild: boolean = false) {}

    public isInEditMode(): boolean {
        return !this.after && !this.before && !this.asChild;
    }
}