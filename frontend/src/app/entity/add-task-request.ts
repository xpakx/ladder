export interface AddTaskRequest {
    title: string;
    description: string;
    projectOrder: number;
    parentId: number;
    projectId: number;
    priority: number;
    due: Date;
    completedAt: Date;
}