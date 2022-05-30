export interface ProjectRequest {
    name: string;
    color: string;
    parentId: number | null;
    favorite: boolean;
}