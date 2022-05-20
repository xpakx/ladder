export const Codes = {
    addFilterAbove: 0,
    addFilterBelow: 1,
    editFilter: 2,
    addToFavs: 3,
    deleteFilter: 4
}

export const MenuElems = {
    addFilterAbove: { name: "Add filter above", code: Codes.addFilterAbove, icon: "fa-arrow-up" }, 
    addFilterBelow: { name: "Add filter below", code: Codes.addFilterBelow, icon: "fa-arrow-down" }, 
    editFilter: { name: "Edit filter", code: Codes.editFilter, icon: "fa-edit" }, 
    addToFavs: { name: "Add to favorites", code: Codes.addToFavs, icon: "fa-heart-o" },
    deleteFromFavs: { name: "Remove from favorites", code: Codes.addToFavs, icon: "fa-heart" },
    deleteFilter: { name: "Delete filter", code: Codes.deleteFilter, icon: "fa-trash-o" }
}
