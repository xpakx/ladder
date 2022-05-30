export const Codes = {
    addLabelAbove: 0,
    addLabelBelow: 1,
    editLabel: 2,
    addToFavs: 3,
    deleteLabel: 4
}

export const MenuElems = {
    addLabelAbove: { name: "Add label above", code: Codes.addLabelAbove, icon: "fa-arrow-up" }, 
    addLabelBelow: { name: "Add label below", code: Codes.addLabelBelow, icon: "fa-arrow-down" }, 
    editLabel: { name: "Edit label", code: Codes.editLabel, icon: "fa-edit" }, 
    addToFavs: { name: "Add to favorites", code: Codes.addToFavs, icon: "fa-heart-o" },
    deleteFromFavs: { name: "Remove from favorites", code: Codes.addToFavs, icon: "fa-heart" },
    deleteLabel: { name: "Delete label", code: Codes.deleteLabel, icon: "fa-trash-o" }
}
