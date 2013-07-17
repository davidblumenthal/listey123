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

	console.log("configureItem: newName = " + newName);
	var data = get_data();
	var list = data[listName];

	if (list !== undefined)  {
		if (!("items" in list)) {
			console.log("adding items list to " + listName);
			list["items"] = [];
		}
		var items = list["items"];
		
		var index;
		for (var i = 0; i < items.length; i++) {
		   if (items[i].name === newName) {
		      index = i;
		      break;
		   } 
		}
	
		if (index === undefined) {
			console.log("configureItem: Saving new item " + newName);
			items.push({lastUpdate: now(),
				    name: newName});
			items.sort(sortItemsByName);
	        }//item doesn't exist
	        else {
	        	console.log("configureItem: " + newName + " already exists, not adding again");
	        }//item already exists

		save_data(data);
	}
	else {
		console.log("Current list " + gSelectedList + " is somehow not defined!");
	}
	
	//close the dialog
	$('.ui-dialog').dialog('close');
}


$(document).on('click', '#saveAddItem', function() {
	console.log("Clicked on saveAddItem");
	configureItem();
});
