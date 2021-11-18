export interface Filter {
    id: number;
    name: string;
    color: string;
    searchString: string;
    favorite: boolean;
    generalOrder: number;
    modifiedAt: Date;
}