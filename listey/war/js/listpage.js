
function displayLists() {
    var listNames = getListNames(),
        ulElem,
        liElem,
        aElem,
        itemsList,
        numItems;

    if (listNames.length == 0) {
        $("#lists").html("Click 'Add A List' to create a list");
    }
    else {
        ulElem = $("<ul>", {"data-role":"listview", "data-count-theme":"c", "data-inset":"true"});

        $.each(listNames, function (index, listName) {
            liElem = $("<li>");
            ulElem.append(liElem);
            itemsList = getItems(listName);
            numItems = keys(itemsList).length;
            aElem = $("<a href='items.html?list="+encodeURIComponent(listName)+"'>" + escapeHTML(listName) + "<span class='ui-li-count'>" + numItems + "</span></a>");
            liElem.append(aElem);
            aElem.click(function () {
                console.log("displayLists: clicked on " + listName);
                //displayItems(value);
                return true;
            });//aElem.click
        });//each

        //replace the current lists div contents with the new unordered list
        $("#lists").html(ulElem);

        //have to explicitly transform to listview after initial page load
        ulElem.listview();
    }
}//displayLists

$('#choose-list-page').bind('pageinit', function(event) {
	if (!isLoggedIn()) {
		handleNotLoggedIn();
	}
	else {
		console.log("User is logged in.");
	}
});


$('#choose-list-page').bind('pagebeforeshow', function() {
	console.log("Showing list page");
	displayLists();
});