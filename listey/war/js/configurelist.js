
function configureList() {
    console.log("configureList - top\n");

    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var origListName = urlVars[LIST_NAME];

    var elem = $("#listName");
    if (elem === undefined) {
        alert("Can't find listName element");
        return;
    }
    var newName = trim(elem.val());

    if (newName.length == 0) {
        alert("You didn't enter a list name");
        return false;
    }

    if (origListName.length>0) {
        if (origListName === newName) {
            console.log("configureListName: name didn't change");
        }
        else {//name changed
        	var list = getList(user, listId, origListName);
        	list[LAST_UPDATE] = now();
        	list[NAME] = newName;
        	saveData();
        }
    }//if renaming

    else {//if adding new
        if (addList(newName)) {
            //refresh the list of lists
            displayLists();
        }
    }//adding new

    //close the dialog
    $('.ui-dialog').dialog('close');
}//configureList



function deleteList() {
    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var origListName = urlVars[LIST_NAME];
    if (user === getCurrentUser()) {
    	var list = getList(user, listId, origListName);
    	list[LAST_UPDATE] = now();
    	list[STATUS] = DELETED_STATUS;
    	saveData();
    	//close the dialog
        $('.ui-dialog').dialog('close');
    } else {
    	alert("Hiding other user's lists isn't currently supported");
    }
}//deleteList



$(document).on('click', '#saveConfigureList', function() {
    console.log("Clicked on saveConfigureList");
    configureList();
});


$(document).on('click', '#deleteListButton', function() {
	console.log("Clicked on deleteListButton");
	if (confirm("Are you sure you want to delete the list?")) {
		deleteList();
		return false;
	} else {
		return false;
	}
});//clicked delete


var shareListButtonClicked = false;
$(document).on('click', '#shareListButton', function() {
    console.log("Clicked on shareListButton, closing");
    shareListButtonClicked = true;
    $('.ui-dialog').dialog('close');
});


$(document).on('submit', '#config-list-dialog-form', function(eventObject) {
    console.log("config-list-dialog-form submitted");
    configureList();
    eventObject.preventDefault();
    return false;
});

$(document).on('pagebeforeshow', '#configure-list-dialog', function() {
    var urlVars = getUrlVars();
    var user = urlVars[USER];
    var listId = urlVars[LIST_ID];
    var listName = urlVars[LIST_NAME];

    console.log("configure-list-dialog pagebeforeshow: listName=" + listName);
    
    //Set the button text to "Hide" if it's not your list
    $('#deleteListButton').text((user === getCurrentUser()) ? "Delete" : "Hide");
    
    if (listName === undefined) {
        $("#deleteListButton").hide();
        $("#origListName").val("");
    }
    else {
        $("#configureListHeader").text("Rename List");
        $("#origListName").val(listName);
        $("#listName").val(listName);
        $("#deleteListButton").show();
        //Add list parameter to delete link
        $('#deleteListButton').attr("href", 'deleteListConfirm.html?' + makeListUrlParams(user, listId, listName));
    }
});

$(document).on('pagehide','#configure-list-dialog', function() {
	console.log("configure-list-dialog pagehide");
	if (shareListButtonClicked === true) {
		shareListButtonClicked = false;
		console.log("Loading shareList.html");
		$.mobile.changePage("shareList.html", {transition: 'pop', role: 'dialog'});
	}
});