var gConfigureItemName;//Can't pass parameters to dialog boxes, have to pass in global vars

function configureItem() {
    console.log("configureItem - top\n");

    var listName = getUrlVars()["list"];

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
    item["categories"] = categories;

    addOrUpdateItem(listName, item, gConfigureItemName);

    //Sigh, can't pass cgi/location bar params to dialogs, so have to use global
    //So need to wipe the global when the dialog closes.
    gConfigureItemName = undefined;

    //close the dialog
    $('.ui-dialog').dialog('close');
}



function displayCategories(categoriesDivId) {
    console.log("displayCategories - top\n");

    var listName = getUrlVars()["list"];

    //Note, this assumes listName is a valid list
    var categories = getItems(listName, CATEGORIES),
        crossedOffItems = getItems(listName, CROSSED_OFF_ITEMS),
        fieldContainElem, fieldSetElem, inputElem, labelElem;

    if (categories.length == 0) {
        console.log("No stores found for " + listName);
        $("#categories").html("Click 'Configure Stores' to add a store");
    }
    else {
        console.log(categories.length + " categories found");

        var item = getItem(listName, gConfigureItemName);
        var itemCategoriesHash = {};
        if (item !== undefined) {
            itemCategoriesHash = item["categories"];
            console.log("Configuring " + gConfigureItemName);
        }
        else {
            console.log("Adding new item");
        }
        fieldContainElem = $("<div>", {"data-role":"fieldcontain"});
        fieldSetElem = $("<fieldset>", {"data-role":"controlgroup"});
        fieldSetElem.append("<legend>Choose which stores this applies to:</legend>");

        $.each(categories, function (index, value) {
            console.log("   Adding " + value["name"]);
            var attributes = {"type":"checkbox", class:"custom", "id":"checkbox-"+index, "value":value["name"]};
            if (itemCategoriesHash[value["name"]]) {
                attributes["checked"] = "true";
            }
            inputElem = $("<input>", attributes);
            fieldSetElem.append(inputElem);
            labelElem = $("<label>", {"for":"checkbox-"+index});
            labelElem.text(value["name"]);
            fieldSetElem.append(labelElem);
        });//each item
        fieldContainElem.append(fieldSetElem);
        //replace the current lists div contents with the new unordered list
        $("#categories").html(fieldContainElem);

        //have to explicitly transform to pretty view after initial page load
        $("#" + categoriesDivId).trigger('create');
    }//else not empty
}//displayCategories


$(document).on('click', '#saveAddItem', function() {
    console.log("Clicked on saveAddItem");
    configureItem();
});


$(document).on('pagebeforeshow', '#config-item-dialog', function() {
    var listName = getUrlVars()["list"];
    displayCategories("categories");

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