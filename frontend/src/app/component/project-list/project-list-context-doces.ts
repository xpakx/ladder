export const Codes = {
    addProjectAbove: 0,
    addProjectBelow: 1,
    editProject: 2,
    addToFavs: 3,
    duplicate: 4,
    archiveProject: 5,
    deleteProject: 6
}

export const MenuElems = {
    addProjectAbove: { name: "Add project above", code: Codes.addProjectAbove, icon: "fa-arrow-up" }, 
    addProjectBelow: { name: "Add project below", code: Codes.addProjectBelow, icon: "fa-arrow-down" }, 
    editProject: { name: "Edit project", code: Codes.editProject, icon: "fa-edit" }, 
    addToFavs: { name: "Add to favorites", code: Codes.addToFavs, icon: "fa-heart-o" },
    deleteFromFavs: { name: "Remove from favorites", code: Codes.addToFavs, icon: "fa-heart" },
    duplicate: { name: "Duplicate", code: Codes.duplicate, icon: "fa-clone" }, 
    archiveProject: { name: "Archive", code: Codes.archiveProject, icon: "fa-archive" },
    deleteProject: { name: "Delete project", code: Codes.deleteProject, icon: "fa-trash-o" }
}
