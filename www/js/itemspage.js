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
        itemCategories = item["categories"];
        for (var i=0; i < selectedCategories.length; i++) {
            if (itemCategories[selectedCategories[i]]){
                return true;
            }
        }//for
        return false;
    }));
}//filterSelectedCategories


function displayItems (listName) {
    console.log("displayItems for list " + listName);
    if (listName === undefined) {
        listName = gSelectedList;
    } else {
        gSelectedList = listName;
    }

    //Note, this assumes listName is a valid list
    var data = get_data(),
        selectedCategories = getSelectedCategories(listName),
        items = filterSelectedCategories(getItems(listName), selectedCategories),
        crossedOffItems = filterSelectedCategories(getItems(listName, CROSSED_OFF_ITEMS), selectedCategories),
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
                    addOrUpdateItem(listName, item, undefined, CROSSED_OFF_ITEMS);
                    displayItems();
                }
                return true;
            });//aElem.click

            aElem = $("<a href='configureItem.html' data-rel='dialog'>Configure</a>");
            liElem.append(aElem);
            aElem.click(function () {
                gConfigureItemName = value["name"];
                console.log("Clicked " + value["name"] + " configuration");
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

                aElem = $("<a href='configureItem.html' data-rel='dialog'>Configure</a>");
                liElem.append(aElem);
                aElem.click(function () {
                    gConfigureItemName = value["name"];
                    console.log("Clicked " + value["name"] + " configuration");
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
    $('#addItemLink').attr("href", 'configureItem.html?list=' + encodeURIComponent(listName))
                     .click(function() {
                         gConfigureItemName = undefined;
                         console.log("Clicked add new item");
                         return true;
                     });

    //Add list parameter to selectCategoriesLink url
    $('#selectCategoriesLink').attr("href", 'selectCategories.html?list=' + encodeURIComponent(listName));

    //Add list parameter to configCatLink url
    $('#configCatLink').attr("href", 'configureCategory.html?list=' + encodeURIComponent(listName));

    //Add list parameter to configureList link
    $('#configureList').attr("href", 'configureList.html?list=' + encodeURIComponent(listName));
});

