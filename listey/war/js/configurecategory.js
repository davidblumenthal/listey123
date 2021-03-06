
function configureCategory() {

    console.log("configureCategory - top\n");

    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];

    var elem = $("#catName");
    if (elem.length == 0) {
        alert("Can't find catName element");
        return;
    }
    var newName = trim(elem.val());

    if (newName.length == 0) {
        alert("You didn't enter a category name");
        return false;
    }

    console.log("configureCategory: newName = " + newName);
    addCategory(user, listId, listName, newName);

    //close the dialog
    $('.ui-dialog').dialog('close');
}

$(document).on('pagebeforeshow', '#config-cat-dialog', function() {
    var listName = getUrlVars()["list"];

    $('#configCatDialogListInput').val(listName);
});

$(document).on('click', '#saveCategory', function() {
    console.log("Clicked on saveCategory");
    configureCategory();
});

$(document).on('submit', '#configure-cat-dialog-form', function(eventObject) {
    console.log("Form submitted");
    configureCategory();
    eventObject.preventDefault();

    return false;
});