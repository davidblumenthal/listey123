
function configureList() {
	console.log("configureList - top\n");
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

	console.log("configureList: newName = " + newName);
	var data = get_data();
	if (!(newName in data)) {
		console.log("configureList: Saving new list " + newName);
		data[newName] = {lastUpdate: now()
	        	        };
		save_data(data);
		displayLists();
	}
	else {
		console.log("configureList: " + newName + " already exists, not adding again");
	}
	//blank out value so it will be blank on next load
//	$("#listName").val("");

	//close the dialog
	$('.ui-dialog').dialog('close');
}

$(document).on('click', '#saveAddList', function() {
	console.log("Clicked on saveAddList");
	configureList();
});
