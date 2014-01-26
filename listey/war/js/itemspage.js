/*
(string list filteredCategories) filterSelectgedCategories(items, string list selectedCategoriesList)

Return items that have a category that is in the selectedCategoriesList map

If selectedCategoriesList is empty, return everything.

 */
function filterSelectedCategories(items, selectedCategoriesList) {
	console.log("filterSelectedCategories: selectedCategories = " + JSON.stringify(selectedCategoriesList));
	if (selectedCategoriesList.length === 0) {
		return items;
	}

	return ($.grep(items, function(item){
		itemCategories = item[CATEGORIES];
		if (itemCategories !== undefined) {
			for (var i=0; i < selectedCategoriesList.length; i++) {
				var selectedCat = selectedCategoriesList[i];
				if (itemCategories[selectedCat] && itemCategories[selectedCat][STATUS] === ACTIVE_STATUS){
					return true;
				}
			}//for
		}
		return false;
	}));
}//filterSelectedCategories


function displayItems (user, listId, listName) {
	console.log("displayItems for list " + listName + " for " + user);
	if (listName === undefined) {
		listName = gSelectedListName;
		listId = gSelectedListId;
	} else {
		gSelectedListName = listName;
		gSelectedListNameId = listId;
	}

	//Note, this assumes listName is a valid list
	var data = getData(),
	selectedCategoriesList = getSelectedCategoriesAsList(user, listId, listName),
	items = filterSelectedCategories(getItems(user, listId, listName, ACTIVE_STATUS), selectedCategoriesList),
	crossedOffItems = filterSelectedCategories(getItems(user, listId, listName, COMPLETED_STATUS), selectedCategoriesList),
	ulElem,
	liElem,
	aElem,
	itemCountSpan,
	lastUpdateSpan;

	if (items === undefined) {
		items = [];
	}
	if (crossedOffItems === undefined) {
		crossedOffItems = [];
	}

	if ((items.length == 0) && (crossedOffItems.length == 0)) {
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

		$.each(items, function (index, item) {
			console.log("   Adding " + item[NAME]);
			
			liElem = $("<li>");
			ulElem.append(liElem);
			
			lastUpdateSpan="<p class='lastUpdateClass ui-li-aside'>(" + compactDateString(item[LAST_UPDATE]) + ")</p>";
			if (COUNT in item
					&& item[COUNT] != 1) {
				itemCountSpan = "<span class='ui-li-count'>" + item[COUNT] + "</span>";
			}
			else {
				itemCountSpan = "";
			}
			aElem = $("<a href='#'>" + escapeHTML(item[NAME]) + lastUpdateSpan + itemCountSpan + "</a>");
			liElem.append(aElem);
			aElem.click(function () {
				console.log("Clicked " + item[NAME]);
				item[STATUS] = COMPLETED_STATUS;
				addOrUpdateItem(user, listId, listName, item);
				displayItems(user, listId, listName);
				return true;
			});//aElem.click

			aElem = $("<a href='configureItem.html' data-rel='dialog'>Configure</a>");
			liElem.append(aElem);
			aElem.click(function () {
				gConfigureItemName = item[NAME];
				gConfigureItemId = item[UNIQUE_ID];
				console.log("Clicked " + item[NAME] + " configuration");
				return true;
			});//aElem.click
		});//each item

		if (crossedOffItems.length > 0) {
			ulElem.append("<li>Crossed Off</li>", {"data-role":"list-divider"});

			$.each(crossedOffItems, function (index, item) {
				console.log("   Adding crossed off " + item[NAME]);
				liElem = $("<li>");
				ulElem.append(liElem);
				lastUpdateSpan="<p class='lastUpdateClass ui-li-aside'>(" + compactDateString(item[LAST_UPDATE]) + ")</p>";
				if (COUNT in item
						&& item[COUNT] != 1) {
					itemCountSpan = "<span class='ui-li-count'>" + item[COUNT] + "</span>";
				}
				else {
					itemCountSpan = "";
				}
				//XXX use a style, instead of strike
				aElem = $("<a href='#'><strike>" + escapeHTML(item[NAME]) + lastUpdateSpan + itemCountSpan + "<strike></a>");
				liElem.append(aElem);
				aElem.click(function () {
					console.log("Clicked crossed off " + item[NAME]);
					item[STATUS] = ACTIVE_STATUS;
					addOrUpdateItem(user, listId, listName, item);
					displayItems(user, listId, listName);

					return true;
				});//aElem.click

				aElem = $("<a href='configureItem.html' data-rel='dialog'>Configure</a>");
				liElem.append(aElem);
				aElem.click(function () {
					gConfigureItemName = item[NAME];
					gConfigureItemId = item[UNIQUE_ID];
					console.log("Clicked " + item[NAME] + " configuration");
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


$(document).on('pagebeforeshow', '#items-page', function() {
	gConfigureItemName=null;//Just to be sure, force these to null
	gConfigureItemId = null;
	var urlVars = getUrlVars();
	var user = urlVars[USER]
	var listId = urlVars[LIST_ID];
	var listName = urlVars[LIST_NAME];
	
	//In case we just changed the list name, load the list and reload the listname
	var list=getList(user, listId, listName);
	listName = list[NAME];
	
	$('#itemsPageTitle').text(listName);
	displayItems(user, listId, listName);
	var urlParams = makeListUrlParams(user, listId, listName);
	
	console.log("ItemsPage::pagebeforeshow - user=" + user + ", listId=" + listId + ", listName=" + listName);

	//Add list parameter to addItemLink url
	$('#addItemLink').attr("href", 'configureItem.html?' + urlParams)
	.click(function() {
		gConfigureItemName = undefined;
		gConfigureItemId = undefined;
		console.log("Clicked add new item");
		return true;
	});

	//If zero or 1 category is selected, then display a select box.
	//If you long-press on the select box, open the multi-select dialog.
	//If multiple selected, list in button.
	var selectedCategoriesMap = getSelectedCategoriesAsMap(user, listId, listName);
	var categories = getCategories(user, listId, listName);
	var numSelected = keys(selectedCategoriesMap).length;
	if (categories.length) {
		var selectedCatDesc = undefined;
		if (numSelected > 1) {
			var selectedCategoriesNamesList = [];
			for (var i=0; i<categories.length; i++) {
				if (selectedCategoriesMap[categories[i][UNIQUE_ID]]) {
					selectedCategoriesNamesList.push(categories[i][NAME]);
				}
			}
			var firstSelectedCategories = selectedCategoriesNamesList.slice(0, 2);
			var otherSelectedCategories = selectedCategoriesNamesList.slice(2);
			var selectedCatDesc = firstSelectedCategories.join(', ');
			if (otherSelectedCategories.length>0) {
				selectedCatDesc += " + " + otherSelectedCategories.length;
			}
		}//if more than one thing selected
		var selectCatSelectBoxOptions =
			"<select id='selectCatSelectBox' data-inline='true' data-icon='grid'>\n" +
			"<option value=''" + (numSelected ? '>Clear Selection' : ' selected>Only Show Items In...') + "</option>\n";
		var shouldSelect;
		if (selectedCatDesc !== undefined) {
			selectCatSelectBoxOptions += "<option value='multiple_selected' selected>" + escapeHTML(selectedCatDesc) + "</option>\n";
			shouldSelect = {};
		}
		else {
			shouldSelect = selectedCategoriesMap;
		}
		selectCatSelectBoxOptions += "<option value='multiple'>Choose multiple...</option>\n";
		for (var i=0; i<categories.length; i++) {
			selectCatSelectBoxOptions += "<option value='" + categories[i][UNIQUE_ID] + "'" + (shouldSelect[categories[i][UNIQUE_ID]] ? "selected" : "") + ">" + escapeHTML(categories[i][NAME]) + "</option>\n";
		}
		selectCatSelectBoxOptions += "</select>\n";
		$('#selectCatSelectBoxContainer').html(selectCatSelectBoxOptions).trigger('create');
	}//if any categories
	else {
		//If there aren't any categories set up, hide the category select box
		$('#selectCatSelectBox').hide();
	}
	
	//Add list parameter to selectCategoriesLink url
	$('#selectCategoriesLink').attr("href", 'selectCategories.html?' + urlParams);

	//Add list parameter to configCatLink url
	$('#configCatLink').attr("href", 'configureCategory.html?' + urlParams);

	//Add list parameter to configureList link
	$('#configureList').attr("href", 'configureList.html?' + urlParams);
});



$(document).on('change', '#selectCatSelectBox', function() {
    console.log("selectCatSelectBox changed");
    var str = "";
	var urlVars = getUrlVars();
	var user = urlVars[USER]
	var listId = urlVars[LIST_ID];
	var listName = urlVars[LIST_NAME];
	
    $( "#selectCatSelectBox option:selected" ).each(function() {
    	console.log("   selected=" + $( this ).val());
    	var selectedVal = $(this).val();
    	if (selectedVal === "multiple") {
    		console.log("Opening dialog");
    		$('#selectCategoriesLink').click();
    		return false;//don't actually allow it to change
    	}
    	else if (selectedVal === "") {
    	    saveSelectedCategories(user, listId, listName, []);
    	} else {
    		saveSelectedCategories(user, listId, listName, [selectedVal]);
    	}
    });//each
    
    //refresh the page, but do it in a timer so this can complete
    //before we refresh
    setTimeout(function(){
    	$("#items-page").trigger("pagebeforeshow");
    },0);
});