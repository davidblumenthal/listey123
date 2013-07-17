
function configureCategory() {
	var LIST_NAME = "categories";
	console.log("configureCategory - top\n");
	
	var listName = getUrlVars()["list"];
	
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
	var data = get_data();
	var list = data[listName];

	if (list !== undefined)  {
		if (!(LIST_NAME in list)) {
			console.log("adding categories list to " + listName);
			list[LIST_NAME] = [];
		}
		var categories = list[LIST_NAME];
		
		var index;
		for (var i = 0; i < categories.length; i++) {
		   if (categories[i].name === newName) {
		      index = i;
		      break;
		   } 
		}
	
		if (index === undefined) {
			console.log("configureCategories: Saving new category " + newName);
			categories.push({lastUpdate: now(),
				    name: newName});
			categories.sort(sortHashesByName);
	        }//category doesn't exist
	        else {
	        	console.log("configureCategory: " + newName + " already exists, not adding again");
	        }//item already exists

		save_data(data);
	}
	else {
		console.log("Current list " + listName + " is somehow not defined!");
	}
	
	//close the dialog
	$('.ui-dialog').dialog('close');
}

$(document).on('click', '#saveCategory', function() {
	console.log("Clicked on saveCategory");
	configureCategory();
});
