export interface AddTaskRequest {
    title: string;
    description: string;
    projectOrder: number;
    parentId: number | null;
    projectId: number | null;
    priority: number;
    due: Date | null;
    completedAt: Date | null;
}