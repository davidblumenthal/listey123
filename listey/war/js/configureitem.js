var gConfigureItemId;

function configureItem() {
    console.log("configureItem - top\n");

    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];    var urlVars = getUrlVars();
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
    $(".category").each(function() {
    	var catVal = {};
    	catVal[LAST_UPDATE] = now();
    	catVal[STATUS] = $(this).is(':checked') ? ACTIVE_STATUS : DELETED_STATUS;
        categories[$(this).val()] = catVal;
    });

    var item = {name: newName};
    item[ITEM_CATEGORIES] = categories;
    if (gConfigureItemId) {
    	item[UNIQUE_ID] = gConfigureItemId;
    }
    item[COUNT] = $('#itemCountSpan').text();
    addOrUpdateItem(user, listId, listName, item);

    //Sigh, can't pass cgi/location bar params to dialogs, so have to use global
    //So need to wipe the global when the dialog closes.
    gConfigureItemId = undefined;
    gConfigureItemName = undefined;

    //close the dialog
    $('.ui-dialog').dialog('close');
}//configureItem


function hideItem() {
    console.log("hideItem - top\n");

    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];

    var item = getItem(user, listId, listName, gConfigureItemId);
   	item[STATUS] = HIDDEN_STATUS;
   	delete[ITEM_CATEGORIES];
   	delete item[COUNT];

    addOrUpdateItem(user, listId, listName, item);

    //Sigh, can't pass cgi/location bar params to dialogs, so have to use global
    //So need to wipe the global when the dialog closes.
    gConfigureItemId = undefined;
    gConfigureItemName = undefined;

    //close the dialog
    $('.ui-dialog').dialog('close');
}//hideItem


$(document).on('click', '#saveAddItem', function() {
    console.log("Clicked on saveAddItem");
    configureItem();
});


$(document).on('click', '#itemIncreaseCount', function() {
	var span = $('#itemCountSpan');
	var currCount = span.text();
	currCount++;
	span.text(currCount);
	console.log("Increasing count to " + currCount);
});



$(document).on('click', '#itemDecreaseCount', function() {
	var span = $('#itemCountSpan');
	var currCount = span.text();
	if (currCount > 1) {
		currCount--;
		span.text(currCount);
		console.log("Increasing count to " + currCount);
	} else {
		console.log("Not decreasing count below 1");
	}
});



$(document).on('click', '#hideItem', function() {
    console.log("Clicked on hideItem");
    hideItem();
});



$(document).on('pagebeforeshow', '#config-item-dialog', function() {
    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];

    var item = getItem(user, listId, listName, gConfigureItemId, gConfigureItemName);
    var itemCategoriesHash, currCount;
    if (item !== undefined) {
    	$('#hideItem').show();
    	currCount = item[COUNT];
        if (currCount===undefined) {
        	currCount = 1;
        }
        itemCategoriesHash = item[CATEGORIES];
        console.log("Configuring " + gConfigureItemName);
    }
    else {
    	$('#hideItem').hide();
    	currCount = 1;
        console.log("Adding new item");
    }
    if (itemCategoriesHash === undefined) {
    	itemCategoriesHash = {};
    }
    $('#itemCountSpan').text(currCount);
    displayCategories(CATEGORIES, itemCategoriesHash);

    if (gConfigureItemName !== undefined) {
        $("#itemName").val(gConfigureItemName);
    }
});//config-item-dialog on pagebeforeshow



$(document).on('submit', '#config-item-dialog-form', function(eventObject) {
    console.log("Form submitted");
    configureItem();
    eventObject.preventDefault();

    return false;
});