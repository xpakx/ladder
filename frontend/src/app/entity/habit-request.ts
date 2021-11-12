export interface HabitRequest {
    title: string;
    description: string;
    allowPositive: boolean;
    allowNegative: boolean;
    projectId: number | undefined;
    priority: number;
    labelIds: number[];
}