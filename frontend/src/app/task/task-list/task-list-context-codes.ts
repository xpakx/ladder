export const Codes = {
    addTaskAbove: 0,
    addTaskBelow: 1,
    editTask: 2,
    moveToProject: 3,
    schedule: 4,
    priority: 5,
    duplicate: 6,
    archiveTask: 7,
    assign: 8,
    restoreTask: 9,
    deleteTask: 10
}

export const MenuElems = {
    addTaskAbove: { name: "Add task above", code: Codes.addTaskAbove, icon: "fa-arrow-up" }, 
    addTaskBelow: { name: "Add task below", code: Codes.addTaskBelow, icon: "fa-arrow-down" }, 
    editTask: { name: "Edit task", code: Codes.editTask, icon: "fa-edit" }, 
    moveToProject: { name: "Move to project", code: Codes.moveToProject, icon: "fa-arrow-right" }, 
    schedule: { name: "Schedule", code: Codes.schedule, icon: "fa-calendar-minus-o" }, 
    priority: { name: "Priority", code: Codes.priority, icon: "fa-flag-o" }, 
    duplicate: { name: "Duplicate", code: Codes.duplicate, icon: "fa-clone" }, 
    archiveTask: { name: "Archive task", code: Codes.archiveTask, icon: "fa-archive" }, 
    assign: { name: "Assign", code: Codes.assign, icon: "fa-user" }, 
    restoreTask: { name: "Restore task", code: Codes.restoreTask, icon: "fa-archive" }, 
    deleteTask: { name: "Delete task", code: Codes.deleteTask, icon: "fa-trash-o" }
}