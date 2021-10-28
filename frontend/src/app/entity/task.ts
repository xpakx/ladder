export interface Task {
    title: string;
    description: string;
    id: number;
    completed: boolean;
    projectOrder: number;
    dailyViewOrder: number
    due: Date;
    priority: number;
    modifiedAt: Date;
}