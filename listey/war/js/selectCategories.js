function selectCategories() {
    console.log("selectCategories - top\n");

    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];
    
    var categories = [];
    $("#selectCategoriesDiv :checked").each(function() {
        categories.push($(this).val());
    });
    saveSelectedCategories(user, listId, listName, categories);

    //close the dialog
    $('.ui-dialog').dialog('close');
}//selectCategories



$(document).on('click', '#saveSelectCategories', function() {
    console.log("Clicked on saveSelectCategories");
    selectCategories();
});


$(document).on('pagebeforeshow', '#select-categories-dialog', function() {
	var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];
    
    var selectedCategoriesMap = getSelectedCategoriesAsMap(user, listId, listName);
    displayCategories("selectCategoriesDiv", selectedCategoriesMap);
});

$(document).on('submit', '#select-categories-dialog-form', function(eventObject) {
    console.log("Form submitted");
    selectCategories();
    eventObject.preventDefault();
    return false;
});