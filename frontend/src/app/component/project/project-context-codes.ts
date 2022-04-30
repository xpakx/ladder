export const Codes = {
    archiveCompleted: 0,
    loadArchived: 1,
    exportToCsv: 2,
    exportToTxt: 3,
    showCollaborations: 4
}

export const MenuElems = {
    archiveCompleted: { name: "Archive completed tasks", code: Codes.archiveCompleted, icon: "fa-archive" },
    loadArchived: { name: "Load archived tasks", code: Codes.loadArchived, icon: "fa-history" },
    exportToCsv: { name: "Export to csv", code: Codes.exportToCsv, icon: "fa-share" },
    exportToTxt: { name: "Export to TODO.txt", code: Codes.exportToTxt, icon: "fa-share" },
    showCollaborations: { name: "Collaborations", code: Codes.showCollaborations, icon: "fa-users" }
}
