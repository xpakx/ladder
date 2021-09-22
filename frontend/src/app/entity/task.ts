export interface Task {
    title: string;
    description: string;
    id: number;
    completed: boolean;
    projectOrder: number;
    due: Date;
}