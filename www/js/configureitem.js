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
			items.sort(sortHashesByName);
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



function displayCategories() {
	/*         <div  data-role="fieldcontain">
            <fieldset data-role="controlgroup">
                <legend>Choose which categories this applies to:</legend>
                <input type="checkbox" name="checkbox-1a" id="checkbox-1a" class="custom" />
                <label for="checkbox-1a">Central Market</label>
                <input type="checkbox" name="checkbox-1b" id="checkbox-1b" class="custom" />
                <label for="checkbox-1b">Breed & Co</label>
            </fieldset>
        </div>
        */
        console.log("displayCategories - top\n");
		
	var listName = getUrlVars()["list"];
		
	//Note, this assumes listName is a valid list
	var data = get_data(),
		categories = data[listName]["categories"],
		crossedOffItems = data[listName]["crossedOffItems"],
		fieldContainElem, fieldSetElem, inputElem, labelElem;
	
	if (categories === undefined) {
		categories = [];
	}
		
	if (categories.length == 0) {
		console.log("No items found for " + listName);
		$("#categories").html("Click 'Configure Categories' to add a category");
	}
	else {
		console.log(categories.length + " categories found");
		fieldContainElem = $("<div>", {"data-role":"fieldcontain"});
		fieldSetElem = $("<fieldset>", {"data-role":"controlgroup"});
		fieldSetElem.append("<legend>Choose which categories this applies to:</legend>");

		$.each(categories, function (index, value) {
			console.log("   Adding " + value["name"]);;
			inputElem = $("<input>", {"type":"checkbox", class:"custom", "id":"checkbox-"+index});
			fieldSetElem.append(inputElem);
			labelElem = $("<label>", {"for":"checkbox-"+index});
			labelElem.text(value["name"]);
			fieldSetElem.append(labelElem);
	    	});//each item
	    	fieldContainElem.append(fieldSetElem);
		//replace the current lists div contents with the new unordered list
	    	$("#categories").html(fieldContainElem);
console.log("New html = "+ $("#categories").html());
	    
	    	//have to explicitly transform to pretty view after initial page load
	    	$("#categories").trigger('create');
	}
}//displayCategories


$(document).on('click', '#saveAddItem', function() {
	console.log("Clicked on saveAddItem");
	configureItem();
});


$(document).on('pageshow', '#config-item-dialog', function() {
	var listName = getUrlVars()["list"];
	displayCategories(listName);
});

