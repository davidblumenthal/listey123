function displayItems (listName) {
    if (listName === undefined) {
        listName = gSelectedList;
    } else {
        gSelectedList = listName;
    }

    //Note, this assumes listName is a valid list
    var data = get_data(),
        items = getItems(listName),
        crossedOffItems = getItems(listName, CROSSED_OFF_ITEMS),
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
            var item = removeItem(listName, value["name"]);
            if (item !== undefined) {//check just to be safe
                addOrUpdateItem(listName, item, CROSSED_OFF_ITEMS);
                displayItems();
            }
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
                    var item = removeItem(listName, value["name"], CROSSED_OFF_ITEMS);
                    if (item !== undefined) {//check just to be safe
                        addOrUpdateItem(listName, item);
                        displayItems();
                    }

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


$(document).on('pagebeforeshow', '#items-page', function() {
    var listName = getUrlVars()["list"];
    $('#itemsPageTitle').text(listName);
    displayItems(listName);

    //Add list parameter to addItemLink url
    $('#addItemLink').attr("href", 'configureItem.html?list=' + encodeURIComponent(listName));

    //Add list parameter to selectCategoriesLink url
    $('#selectCategoriesLink').attr("href", 'selectCategories.html?list=' + encodeURIComponent(listName));

    //Add list parameter to configCatLink url
    $('#configCatLink').attr("href", 'configureCategory.html?list=' + encodeURIComponent(listName));

    //Add list parameter to configureList link
    $('#configureList').attr("href", 'configureList.html?list=' + encodeURIComponent(listName));
});

