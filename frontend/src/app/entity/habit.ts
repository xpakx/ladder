export interface Habit {
    id: number;
    title: string;
    description: string;
    allowPositive: boolean;
    allowNegative: boolean;
    modifiedAt: Date;
    generalOrder: number;
    priority: number;
}