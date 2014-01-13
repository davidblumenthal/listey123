var gConfigureItemName;//Can't pass parameters to dialog boxes, have to pass in global vars
var gConfigureItemId;

function configureItem() {
    console.log("configureItem - top\n");

    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];

    var elem = $("#itemName");
    if (elem.length == 0) {
        alert("Can't find itemName element");
        return;
    }
    var newName = trim(elem.val());

    if (newName.length == 0) {
        alert("You didn't enter an item name");
        return false;
    }
    var categories = {};
    $("#categories :checked").each(function() {
        categories[$(this).val()] = true;
    });

    var item = {name: newName};
    item[ITEM_CATEGORIES] = categories;
    if (gConfigureItemId) {
    	item[UNIQUE_ID] = gConfigureItemId;
    }
    addOrUpdateItem(user, listId, listName, item);

    //Sigh, can't pass cgi/location bar params to dialogs, so have to use global
    //So need to wipe the global when the dialog closes.
    gConfigureItemId = undefined;
    gConfigureItemName = undefined;

    //close the dialog
    $('.ui-dialog').dialog('close');
}



$(document).on('click', '#saveAddItem', function() {
    console.log("Clicked on saveAddItem");
    configureItem();
});


$(document).on('pagebeforeshow', '#config-item-dialog', function() {
    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];

    var item = getItem(user, listId, listName, gConfigureItemId, gConfigureItemName);
    var itemCategoriesHash = {};
    if (item !== undefined) {
        itemCategoriesHash = item[CATEGORIES];
        console.log("Configuring " + gConfigureItemName);
    }
    else {
        console.log("Adding new item");
    }

    displayCategories(CATEGORIES, itemCategoriesHash);

    if (gConfigureItemName !== undefined) {
        $("#itemName").val(gConfigureItemName);
    }
});

$(document).on('submit', '#config-item-dialog-form', function(eventObject) {
    console.log("Form submitted");
    configureItem();
    eventObject.preventDefault();

    return false;
});