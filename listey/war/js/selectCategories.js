function selectCategories() {
    console.log("selectCategories - top\n");

    var listName = getUrlVars()["list"];
    var categories = [];
    $("#selectCategoriesDiv :checked").each(function() {
        categories.push($(this).val());
    });
    saveSelectedCategories(listName, categories);

    //close the dialog
    $('.ui-dialog').dialog('close');
}//selectCategories



$(document).on('click', '#saveSelectCategories', function() {
    console.log("Clicked on saveSelectCategories");
    selectCategories();
});


$(document).on('pagebeforeshow', '#select-categories-dialog', function() {
    var listName = getUrlVars()["list"];
    var selectedCategoriesList = getSelectedCategories(listName);
    var selectedCategoriesHash = {};
    $.each(selectedCategoriesList, function(index, value){
        selectedCategoriesHash[value] = true;
    });
    displayCategories("selectCategoriesDiv", selectedCategoriesHash);
});

$(document).on('submit', '#select-categories-dialog-form', function(eventObject) {
    console.log("Form submitted");
    selectCategories();
    eventObject.preventDefault();
    return false;
});