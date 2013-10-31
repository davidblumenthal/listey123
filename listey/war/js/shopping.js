/*
  Data format:
  "lastUpdate": "1234567890",
  "lists" :
    listName {"items" : [{"name" : "Milk",
                           "lastUpdate": "1234567890",
                          },
                          ],
                 "crossedOffItems" : [{"name" : "Butter",
                                     "count": 2,
                                     "lastUpdate": "1234567890",
                                    },
                                   ],
                 "deletedItems" : [{"name" : "Eggs",
                                  "count": 2,
                                  "lastUpdate": "1234567890",
                                 },
                                ],
                 "purgedItems" : [{"name" : "Doritos",
                                 "count": 2,
                                 "lastUpdate": "1234567890",
                                },
                               ],

                 lastUpdate: "1234567890"
                }

*/

var LISTS = 'lists';
var ITEMS = 'items';
var CROSSED_OFF_ITEMS = 'crossedOffItems';
var CATEGORIES = 'categories';
//var LISTEY_HOME = "http://1.blumenthal-listey.appspot.com/";
//var LISTEY_HOME = "http://localhost:8888/";
var LISTEY_HOME = "";//use a relative link for now, until I'm ready to deploy on phonegap

var LOCALSTORAGE_LISTS_KEY = 'lists';

var gSelectedList, gData={};


//http://stackoverflow.com/questions/5639346/shortest-function-for-reading-a-cookie-in-javascript
function read_cookie(key)
{
    var result;
    return (result = new RegExp('(?:^|; )' + encodeURIComponent(key) + '=([^;]*)').exec(document.cookie)) ? (result[1]) : null;
}

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
        field = LOCALSTORAGE_LISTS_KEY;
    }
    if (data === undefined) {
        data = gData[field];
    }
    else {
        gData[field] = data;
    }
    data["lastUpdate"] = now();
    localStorage.setItem(field, JSON.stringify(data));
    syncData();
}//save_data


function isLoggedIn() {
	return read_cookie("isLoggedIn");
}


function get_data(field) {
    if (field === undefined) {
        field = LOCALSTORAGE_LISTS_KEY;
    }

    if (gData[field] === undefined) {
        var data_str = localStorage.getItem(field);
        //console.log("get_data: field = " + field + ", data_str = " + data_str);
        if (data_str == undefined) {
            data_str = "{}";
        }
        gData[field] = JSON.parse(data_str);
        syncData();
    }
    return gData[field];
}


function handleNotLoggedIn(){
	console.log("The user is not logged in, redirect to the login page");
	window.location = LISTEY_HOME + "login.jsp";
}


/** Post any local changes to the server and
 * get back the latest data stored on the server.
 * XXX - NOTE! This is currently pretty brute-force.  It sends
 * the current data hash to the server, which overwrites its
 * copy if the sent data is newer than its data. Then it takes
 * the response and just compares
 * the timestamp of the entire data hash and overwrites everything
 * if the server timestamp is newer than the local one.
 */ 
function syncData() {
console.log("syncData - top");
	var sentData = get_data();
	var sentLastUpdateDate = sentData["lastUpdate"];
	var params = {"content" : JSON.stringify(sentData),
			      "lastUpdate" : sentLastUpdateDate};
	$.ajax({
		type: "POST",
		url: LISTEY_HOME + "ajax",
		data: params,
		success: function(returnedData){
			console.log("syncData call returned: " + returnedData);
			var returnedDataHash = JSON.parse(returnedData);
			if ((!sentLastUpdateDate && returnedDataHash["lastUpdate"]) 
					|| returnedDataHash["lastUpdate"] > sentLastUpdateDate) {
				console.log("saving data");
				save_data(returnedDataHash);
				window.alert("Pulled an update from the server");
				//just refresh everything.  Note, this could be nicer!
				window.location = "index.html";
			}
			else {
				console.log("Client version is newer than server, not updating");
			}
		},
		statusCode: {
			403: function() {
				handleNotLoggedIn();
			}
		}
	});//ajax
}//syncData



function addList(newName) {
    var didAdd = false;

    var data = get_data();
    if (!(LISTS in data)) {
    	data[LISTS] = {};
    }
    var lists = data[LISTS];
    if (!(newName in lists)) {
        console.log("configureList: Saving new list " + newName);
        lists[newName] = {lastUpdate: now()
                            };
        save_data();
        didAdd = true;
    }
    else {
        console.log("configureList: " + newName + " already exists, not adding again");
    }
    return didAdd;
}//addList


function getList(listName) {
    var data = get_data(), list;
    if (!(LISTS in data)) {
    	data[LISTS] = {};
    }
    var lists = data[LISTS];
    if (!(listName in lists)) {
        lists[listName] = [];
    }

    return (lists[listName]);
}//getList


function getListNames() {
    var data = get_data();
    var listNames = (LISTS in data) ? keys(data[LISTS]).sort() : [];

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


function addCategory(listName, newName) {
	var list = getList(listName);

	if (list !== undefined)  {
		if (!(CATEGORIES in list)) {
			console.log("adding " + CATEGORIES + " list to " + listName);
			list[CATEGORIES] = [];
		}
		var categories = list[CATEGORIES];

		var index;
		for (var i = 0; i < categories.length; i++) {
			if (categories[i].name === newName) {
				index = i;
				break;
			}
		}

		if (index === undefined) {
			console.log("addCategory: Saving new category " + newName);
			categories.push({lastUpdate: now(),
				name: newName});
			categories.sort(sortHashesByName);
		}//category doesn't exist
		else {
			console.log("configureCategory: " + newName + " already exists, not adding again");
		}//item already exists

		save_data();
	}
	else {
		console.log("Current list " + listName + " is somehow not defined!");
	}
}//addCategory


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
