/*
  Data format:
    listName => {items : [{name : "Milk",
                           lastUpdate: "06/28/2013 13:25:29 CDT",
                          },
                          ],
                 crossedOffItems : [{name : "Butter",
                                     count: 2,
                                     lastUpdate: "06/28/2013 13:25:29 CDT",
                                    },
                                   ],
                 deletedItems : [{name : "Eggs",
                                  count: 2,
                                  lastUpdate: "06/28/2013 13:25:29 CDT",
                                 },
                                ],
                 purgedItems : [{name : "Doritos",
                                 count: 2,
                                 lastUpdate: "06/28/2013 13:25:29 CDT",
                                },
                               ],

                 lastUpdate: "06/28/2013 13:25:29 CDT"
                }

*/

var ITEMS = 'items';
var CROSSED_OFF_ITEMS = 'crossedOffItems';
var CATEGORIES = 'categories';

var gSelectedList, gData={};

function sortHashesByName (a, b) {
    a = a["name"].toUpperCase();
    b = b["name"].toUpperCase();
    return ((a < b) ? -1 : (a > b) ? +1 : 0);
}//sortItemsByName


function trim (str) {
    return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

function now(){
    return new Date().getTime();
}


function keys(obj)
{
    var keys = [];

    for(var key in obj)
    {
        if(obj.hasOwnProperty(key))
        {
            keys.push(key);
        }
    }

    return keys;
}


function escapeHTML( string )
{
    var pre = document.createElement('pre');
    var text = document.createTextNode( string );
    pre.appendChild(text);
    return pre.innerHTML;
}


function save_data(data, field) {
    if (field === undefined) {
        field = 'lists';
    }
    if (data === undefined) {
        data = gData[field];
    }
    else {
        gData[field] = data;
    }
    localStorage.setItem(field, JSON.stringify(data));
}//save_data



function get_data(field) {
    if (field === undefined) {
        field = 'lists';
    }

    if (gData[field] === undefined) {
        var data_str = localStorage.getItem(field);
        //console.log("get_data: field = " + field + ", data_str = " + data_str);
        if (data_str == undefined) {
            return {};
        }
        gData[field] = JSON.parse(data_str);
    }
    return gData[field];
}


function addList(newName) {
    var didAdd = false;

    var data = get_data();
    if (!(newName in data)) {
        console.log("configureList: Saving new list " + newName);
        data[newName] = {lastUpdate: now()
                            };
        save_data(data);
        didAdd = true;
    }
    else {
        console.log("configureList: " + newName + " already exists, not adding again");
    }
    return didAdd;
}//addList


function getList(listName) {
    var data = get_data(), list;
    if (!(listName in data)) {
        data[listName] = [];
    }

    return (data[listName]);
}//getList


function getListNames() {
    var data = get_data();
    var listNames = keys(data).sort();

    return listNames;
}//getListNames


function getItems(listName, itemsType) {
    var list = getList(listName);
    if (itemsType === undefined) {
        itemsType = ITEMS;
    }
    if (!(itemsType in list)) {
        list[itemsType] = [];
    }
    return (list[itemsType]);
}//getItems


function getItemIndex(items, itemName) {
    for (var i = 0; i < items.length; i++) {
        if (items[i].name === itemName) {
            return i;
        }
    }
    return -1;
}//getItemIndex


/*
  (itemObj) getItem(listName, itemName, [itemsType])

  itemsType is optional.  If not passed, will look in main
  list and crossed off list.
*/
function getItem(listName, itemName, itemsType) {
    var items = getItems(listName, itemsType);

    var itemIndex = getItemIndex(items, itemName);

    if (itemIndex === -1 && itemsType === undefined) {
        return(getItem(listName, itemName, CROSSED_OFF_ITEMS));
    }
    return (itemIndex === -1 ? undefined : items[itemIndex]);
}//getItem



/*
  (boolean didAddItem) addOrUpdateItem(listName, item, [origItemName], [listType])

  If origItemName is undef, assumes item["name"]

  If listType is defined, only works with that list.
  Otherwise, looks up the item by origItemName in main and crossed off lists.

  If found, updates with info in item.  Otherwise adds to listType or main list.
*/
function addOrUpdateItem(listName, item, origItemName, listType) {
    if (!("name" in item)) {
        console.log("addItem: item parameter doesn't have a name field, skipping");
        return false;
    }
    if (origItemName === undefined) {
        origItemName = item["name"];
    }
    item["lastUpdate"] = now();

    //get it in main list or crossed off list.  If not found in either
    //then add to main list
    var items = getItems(listName, listType);
    var itemIndex = getItemIndex(items, origItemName);
    if (itemIndex === -1 && listType === undefined) {
        var crossedOffItems = getItems(listName, CROSSED_OFF_ITEMS);
        itemIndex = getItemIndex(crossedOffItems, origItemName);
        if (itemIndex !== -1) {
            items = crossedOffItems;
        }
    }

    var didAddItem;

    if (itemIndex === -1) {
        console.log("addItem: Saving new item " + item["name"]);
        items.push(item);
        items.sort(sortHashesByName);
        didAddItem = true;
    }//item doesn't exist
    else {
        console.log("addItem: " + item["name"] + " already exists, updating instead");
        items[itemIndex] = item;
        didAddItem = false;
    }//item already exists

    save_data();

    return didAddItem;
}//addOrUpdateItem



//(addedItem) removeItem(listName, itemName, itemsType)
function removeItem(listName, itemName, itemsType) {
    var items = getItems(listName, itemsType);
    var itemIndex = getItemIndex(items, itemName);

    var removedItem;
    if (itemIndex !== -1) {
        console.log("removeItem: removing " + itemName);
        var itemsSpliced = items.splice(itemIndex, 1);
        removedItem = itemsSpliced[0];
        save_data();
    }//item doesn't exist

    return removedItem;
}//removeItem


function getSelectedCategories(listName) {
    var list = getList(listName);
    var currentFilterCategories = list["selectedCategories"];
    return (currentFilterCategories === undefined ? {} : currentFilterCategories);
}



function saveSelectedCategories(listName, categories) {
    var list = getList(listName);
    list["selectedCategories"] = categories;
    save_data();
}


function displayCategories(categoriesDivId, selectedCategoriesHash) {
    console.log("displayCategories - top\n");

    var listName = getUrlVars()["list"];

    //Note, this assumes listName is a valid list
    var categories = getItems(listName, CATEGORIES),
        crossedOffItems = getItems(listName, CROSSED_OFF_ITEMS),
        fieldContainElem, fieldSetElem, inputElem, labelElem;

    if (categories.length == 0) {
        console.log("No stores found for " + listName);
        $("#" + categoriesDivId).html("Click 'Configure Stores' to add a store");
    }
    else {
        console.log(categories.length + " categories found");

        fieldContainElem = $("<div>", {"data-role":"fieldcontain"});
        fieldSetElem = $("<fieldset>", {"data-role":"controlgroup"});
        fieldSetElem.append("<legend>Choose which stores this applies to:</legend>");

        $.each(categories, function (index, value) {
            console.log("   Adding " + value["name"]);
            var attributes = {"type":"checkbox", class:"custom", "id":"checkbox-"+index, "value":value["name"]};
            if (selectedCategoriesHash[value["name"]]) {
                attributes["checked"] = "true";
            }
            inputElem = $("<input>", attributes);
            fieldSetElem.append(inputElem);
            labelElem = $("<label>", {"for":"checkbox-"+index});
            labelElem.text(value["name"]);
            fieldSetElem.append(labelElem);
        });//each item
        fieldContainElem.append(fieldSetElem);
        //replace the current lists div contents with the new unordered list
        $("#" + categoriesDivId).html(fieldContainElem);

        //have to explicitly transform to pretty view after initial page load
        $("#" + categoriesDivId).trigger('create');
    }//else not empty
}//displayCategories



function getUrlVars() {
    var vars = {}, keyval;
    var keyvals = window.location.href.slice(window.location.href.indexOf('?') + 1).split(/[&#]/);
    for(var i = 0; i < keyvals.length; i++)
    {
        keyval = keyvals[i].split('=');
        vars[decodeURIComponent(keyval[0])] = decodeURIComponent(keyval[1]);
    }
    return vars;
}
