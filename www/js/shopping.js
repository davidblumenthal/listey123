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


function getItem(listName, itemName, itemsType) {
    var items = getItems(listName, itemsType);

    var itemIndex = getItemIndex(items, item["name"]);
    return (itemIndex === -1 ? undefined : items[itemIndex]);
}//getItem



//(didAddItem) addOrUpdateItem(listName, item, itemsType)
function addOrUpdateItem(listName, item, itemsType) {
    if (!("name" in item)) {
        console.log("addItem: item parameter doesn't have a name field, skipping");
        return false;
    }
    item["lastUpdate"] = now();

    var items = getItems(listName, itemsType);
    var itemIndex = getItemIndex(items, item["name"]);

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
