
function displayLists() {
    var listOfLists,
        ulElem,
        liElem,
        aElem,
        itemsList,
        numItems,
        users = getUsers();
    
    for (var i=0; i<users.length; i++) {
    	var user = users[i];
    	listOfLists = getListOfLists(user);
    	if (users.length === 1
    			&& listOfLists.length == 0
    				&& user === getCurrentUser()) {
    		$("#lists").html("Click 'Add A List' to create a list");
    	}
    	else {
    		//XXX ADD USERNAME HEADER IF HAVE ACCESS TO OTHER USER
    		ulElem = $("<ul>", {"data-role":"listview", "data-count-theme":"c", "data-inset":"true"});

    		$.each(listOfLists, function (index, list) {
    			var listName = list.name, listId = list.uniqueId;
    			liElem = $("<li>");
    			ulElem.append(liElem);
    			itemsList = getItems(user, listId, listName);
    			numItems = keys(itemsList).length;
    			aElem = $("<a href='items.html?" + USER + "="+encodeURIComponent(user)+"&" + LIST_ID + "=" + encodeURIComponent(listId) + "&" + LIST_NAME + "=" + encodeURIComponent(listName) + "'>" + escapeHTML(listName) + "<span class='ui-li-count'>" + numItems + "</span></a>");
    			liElem.append(aElem);
    			aElem.click(function () {
    				console.log("displayLists: clicked on " + listName + " for user " + user);
    				//displayItems(value);
    				return true;
    			});//aElem.click
    		});//each
    	}//else
    }//foreach users
    
    if (ulElem) {
    	//replace the current lists div contents with the new unordered list
    	$("#lists").html(ulElem);

    	//have to explicitly transform to listview after initial page load
    	ulElem.listview();
    }
}//displayLists

$('#choose-list-page').bind('pageinit', function(event) {
	var userEmail = loggedInAs();
	if (!userEmail) {
		handleNotLoggedIn();
	}
	else {
		console.log("User is logged in as " + userEmail);
	}
});


$('#choose-list-page').bind('pagebeforeshow', function() {
	console.log("Showing list page");
	displayLists();
});