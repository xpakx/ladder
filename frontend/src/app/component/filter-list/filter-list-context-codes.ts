export const Codes = {
    addFilterAbove: 0,
    addFilterBelow: 1,
    editFilter: 2,
    addToFavs: 3,
    deleteFilter: 4
}

export const MenuElems = {
    addFilterAbove: { name: "Add label above", code: Codes.addFilterAbove, icon: "fa-arrow-up" }, 
    addFilterBelow: { name: "Add label below", code: Codes.addFilterBelow, icon: "fa-arrow-down" }, 
    editFilter: { name: "Edit label", code: Codes.editFilter, icon: "fa-edit" }, 
    addToFavs: { name: "Add to favorites", code: Codes.addToFavs, icon: "fa-heart-o" },
    deleteFromFavs: { name: "Remove from favorites", code: Codes.addToFavs, icon: "fa-heart" },
    deleteFilter: { name: "Delete label", code: Codes.deleteFilter, icon: "fa-trash-o" }
}
