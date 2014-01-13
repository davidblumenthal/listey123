/*
(string list filteredCategories) filterSelectgedCategories(items, string list selectedCategoriesList)

Return items that have a category that is in the selectedCategories list

If selectedCategoriesList is empty, return everything.

 */
function filterSelectedCategories(items, selectedCategories) {
	console.log("filterSelectedCategories: selectedCategories = " + JSON.stringify(selectedCategories));
	if ($.isEmptyObject(selectedCategories)) {
		return items;
	}

	return ($.grep(items, function(item){
		itemCategories = item[CATEGORIES];
		for (var i=0; i < selectedCategories.length; i++) {
			if (itemCategories[selectedCategories[i]]){
				return true;
			}
		}//for
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
	selectedCategories = getSelectedCategories(user, listId, listName),
	items = filterSelectedCategories(getItems(user, listId, listName, ACTIVE_STATUS), selectedCategories),
	crossedOffItems = filterSelectedCategories(getItems(user, listId, listName, COMPLETED_STATUS), selectedCategories),
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
			if (COUNT in item) {
				itemCountSpan = "<span class='ui-li-count'>" + item[COUNT] + "</span>";
			}
			else {
				itemCountSpan = "";
			}
			aElem = $("<a href='#'>" + escapeHTML(item[NAME]) + itemCountSpan + "</a>");
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
				if (COUNT in item) {
					itemCountSpan = "<span class='ui-li-count'>" + item[COUNT] + "</span>";
				}
				else {
					itemCountSpan = "";
				}
				//XXX use a style, instead of strike
				aElem = $("<a href='#'><strike>" + escapeHTML(item[NAME]) + itemCountSpan + "<strike></a>");
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
	var urlVars = getUrlVars();
	var user = urlVars[USER]
	var listId = urlVars[LIST_ID];
	var listName = urlVars[LIST_NAME];
	$('#itemsPageTitle').text(listName);
	displayItems(user, listId, listName);
	var urlParams = USER + '=' + encodeURIComponent(user) + '&' + LIST_ID + '=' + encodeURIComponent(listId) + '&' + LIST_NAME + '=' + encodeURIComponent(listName);

	//Add list parameter to addItemLink url
	$('#addItemLink').attr("href", 'configureItem.html?' + urlParams)
	.click(function() {
		gConfigureItemName = undefined;
		gConfigureItemId = undefined;
		console.log("Clicked add new item");
		return true;
	});

	//Add list parameter to selectCategoriesLink url
	$('#selectCategoriesLink').attr("href", 'selectCategories.html?' + urlParams);

	//Add list parameter to configCatLink url
	$('#configCatLink').attr("href", 'configureCategory.html?' + urlParams);

	//Add list parameter to configureList link
	$('#configureList').attr("href", 'configureList.html?' + urlParams);
});

