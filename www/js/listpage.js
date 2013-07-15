$('#choose-list-page').bind('pageinit', function(event) {
	displayLists();
});

function displayLists() {
	var data = get_data(),
		listNames = keys(data).sort(),
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

		$.each(listNames, function (index, value) {
			liElem = $("<li>");
			ulElem.append(liElem);
			itemsList = data[value]["items"];
			numItems = (itemsList === undefined) ? 0 : keys(itemsList).length;
			aElem = $("<a href='items?list="+encodeURIComponent(value)+"'>" + escapeHTML(value) + "<span class='ui-li-count'>" + numItems + "</span></a>");
			liElem.append(aElem);
			aElem.click(function () {
			    console.log("displayLists: clicked on " + value);
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