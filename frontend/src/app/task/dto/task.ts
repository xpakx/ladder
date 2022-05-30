export interface Task {
    title: string;
    description: string;
    id: number;
    completed: boolean;
    collapsed: boolean;
    projectOrder: number;
    dailyViewOrder: number
    due: Date;
    timeboxed: boolean;
    priority: number;
    modifiedAt: Date;
}