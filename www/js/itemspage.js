function displayItems (listName) {
	if (listName === undefined) {
		listName = gSelectedList;
	} else {
		gSelectedList = listName;
	}
	
	//Note, this assumes listName is a valid list
	var data = get_data(),
		items = data[listName]["items"],
		crossedOffItems = data[listName]["crossedOffItems"],
		ulElem,
		liElem,
		aElem,
		itemCountSpan;

	if (items === undefined) {
	    items = [];
	}
	if (crossedOffItems === undefined) {
	    crossedOffItems = [];
	}
	
	if ((items === undefined || items.length == 0) && (crossedOffItems === undefined || crossedOffItems.length == 0)) {
	    console.log("No items found for " + listName);
	    $("#items").html("Click 'Add An Item' to add an item");
	}
	else {
	/*
	                <li>
	                	<a href="#">Milk</a>
	                	<span class="ui-li-count">2</span>
	                	<a href="#config-item-dialog" data-rel="dialog">Configure</a>
	                </li>
	                <li>
	                	<a href="#" data-rel="dialog">Pepper</a>
	                	<a href="#config-item-dialog" data-rel="dialog">Configure</a>
	                </li>
	                <li data-role="list-divider" dividerTheme="a" >Crossed off</li>
	                <li>
					  	<a href="#" data-rel="dialog"><strike>Salt</strike></a>
					    <a href="#config-item-dialog" data-rel="dialog">Configure</a>
	                </li>
*/
	    console.log("Some items or crossedOffItems found: items=" + items.length + ", crossedOffItems=" + crossedOffItems.length);
	    ulElem = $("<ul>", {"data-role":"listview", "data-split-icon":"gear", "data-count-theme":"c", "data-inset":"true"});

	    $.each(items, function (index, value) {
	        console.log("   Adding " + value["name"]);;
		liElem = $("<li>");
		ulElem.append(liElem);
		if ("count" in value) {
		    itemCountSpan = "<span class='ui-li-count'>" + value["count"] + "</span>";
		}
		else {
		    itemCountSpan = "";
		}
		aElem = $("<a href='#'>" + escapeHTML(value["name"]) + itemCountSpan + "</a>");
		liElem.append(aElem);
		aElem.click(function () {
		    console.log("Clicked " + value["name"]);
		    if (crossedOffItems.length == 0) {
		    	console.log("initializing crossedOffItems");
		    	data[listName]["crossedOffItems"] = crossedOffItems;
		    }
		    var itemsSpliced = items.splice(index, 1);
		    itemsSpliced[0]["lastUpdate"] = now();
		    crossedOffItems.push(itemsSpliced[0]);
		    crossedOffItems.sort(sortHashesByName);
		    save_data(data);
		    displayItems();
		    return true;
		});//aElem.click
			
		aElem = $("<a href='#config-item-dialog' data-rel='dialog'>Configure</a>");
		liElem.append(aElem);
		aElem.click(function () {
		    alert("Clicked " + value["name"] + " configuration");
		    return true;
		});//aElem.click
	    });//each item
		
	    if (crossedOffItems.length > 0) {
		ulElem.append("<li>Crossed Off</li>", {"data-role":"list-divider"});
		
		$.each(crossedOffItems, function (index, value) {
			        console.log("   Adding crossed off " + value["name"]);;
				liElem = $("<li>");
				ulElem.append(liElem);
				if ("count" in value) {
				    itemCountSpan = "<span class='ui-li-count'>" + value["count"] + "</span>";
				}
				else {
				    itemCountSpan = "";
				}
				//XXX use a style, instead of strike
				aElem = $("<a href='#'><strike>" + escapeHTML(value["name"]) + itemCountSpan + "<strike></a>");
				liElem.append(aElem);
				aElem.click(function () {
		    			console.log("Clicked crossed off " + value["name"]);
					if (items.length == 0) {
					  	console.log("initializing items");
					    	data[listName]["items"] = items;
					}
					var itemsSpliced = crossedOffItems.splice(index, 1);
					itemsSpliced[0]["lastUpdate"] = now();
					items.push(itemsSpliced[0]);
					items.sort(sortHashesByName);
					save_data(data);
					displayItems();
					return true;
				});//aElem.click
					
				aElem = $("<a href='configureItem' data-rel='dialog'>Configure</a>");
				liElem.append(aElem);
				aElem.click(function () {
				    alert("Clicked " + value["name"] + " configuration");
				    return true;
				});//aElem.click
	    	});//each item
	    }//crossedOffItems

	    //replace the current lists div contents with the new unordered list
	    $("#items").html(ulElem);

	    //have to explicitly transform to listview after initial page load
	    $("#items").trigger('create');
	}
}//displayItems


$(document).on('pageshow', '#items-page', function() {
	var listName = getUrlVars()["list"];
	displayItems(listName);
	
	//Add list parameter to addItemLink url
	$('#addItemLink').attr("href", 'configureItem?list=' + encodeURIComponent(listName));
		
	//Add list parameter to configCatLink url
	$('#configCatLink').attr("href", 'configureCategory?list=' + encodeURIComponent(listName));
});

