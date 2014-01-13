/*
  Data format:
  
  {
  	    "lastUpdate": "1234567890",
  		"lists" : {
    		'self' : {
    		     <listName> : {
    		         "items" :
    		               [{"name" : "<NAME1>",
    		                 "categories" : {"<CATEGORY1>" : {status:"XXX", lastUpdate:"342324134"}, ...},
    		                 "count" : <NUMBER>,
                             "lastUpdate": "1234567890",
                            },
                            ...
                           ],
                     "crossedOffItems" : [... SEE "items" above]
                     "lastUpdate": "1234567890",
                     "categories": [{name="<CATEGORY_NAME>", "lastUpdate"=123456789}, ...],
                     "selectedCategories" : ["<CATEGORY1>", ...]
                 }//<listName>
            },
            '<OTHER_USER_EMAIL>' : {... see "self" above}
        }//lists
  }//top-level
*/

var USER_DATA = 'userData';
var LAST_UPDATE = 'lastUpdate';
var NAME = 'name';
var STATUS = 'status';
var LISTS = 'lists';
var ITEMS = 'items';
var CROSSED_OFF_ITEMS = 'crossedOffItems';
var CATEGORIES = 'categories';
var SELECTED_CATEGORIES = 'selectedCategories';
var OTHER_USER_PRIVS = 'otherUserPrivs';
var COUNT = 'count';
var LIST_ID = 'listId';
var LIST_NAME = 'listName';

//Possible privs to grant to other users
var FULL_PRIV = 'FULL';
var VIEW_PRIV = 'VIEW';

//Possible node statuses
var ACTIVE_STATUS = 'ACTIVE';
var COMPLETED_STATUS = 'COMPLETED';
var HIDDEN_STATUS = 'HIDDEN';
var DELETED_STATUS = 'DELETED';

//var LISTEY_HOME = "http://1.blumenthal-listey.appspot.com/";
//var LISTEY_HOME = "http://localhost:8888/";
var LISTEY_HOME = "";//use a relative link for now, until I'm ready to deploy on phonegap

var LOCALSTORAGE_LISTS_KEY = 'lists';
var LOCALSTORAGE_CURRENT_USER = 'current_user';

var gSelectedList, gData={};

//Parse the cookies for the site for the given cookie name (key)
//http://stackoverflow.com/questions/5639346/shortest-function-for-reading-a-cookie-in-javascript
function read_cookie(key)
{
    var result;
    return (result = new RegExp('(?:^|; )' + encodeURIComponent(key) + '=([^;]*)').exec(document.cookie)) ? (result[1]) : null;
}

//compare object a and b by NAME property
function compareHashesByName (a, b) {
    a = a[NAME].toUpperCase();
    b = b[NAME].toUpperCase();
    return ((a < b) ? -1 : (a > b) ? +1 : 0);
}//compareHashesByName


//Trim off leading and trailing spaces and return the trimmed string
function trim (str) {
    return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}


//Returns the milliseconds since 1970 UTC as a long integer
function now(){
    return new Date().getTime();
}


//Returns a list of the proper keys of the passed object (using hasOwnProperty)
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
}//keys


//HTML-Escape the passed string, so the browser will display
//the characters exactly like originally passed
function escapeHTML( string )
{
    var pre = document.createElement('pre');
    var text = document.createTextNode( string );
    pre.appendChild(text);
    return pre.innerHTML;
}


//Read the window location, parse the url and return the url vars
//as an object
function getUrlVars() {
    var vars = {}, keyval;
    var keyvals = window.location.href.slice(window.location.href.indexOf('?') + 1).split(/[&#]/);
    for(var i = 0; i < keyvals.length; i++)
    {
        keyval = keyvals[i].split('=');
        vars[decodeURIComponent(keyval[0])] = decodeURIComponent(keyval[1]);
    }
    return vars;
}//getUrlVars


//Get an temporary unique id.  The server thinks anything
//that starts with a colon is a temporary, server-side-generated
//unique ID.  It will replace that in its response with the server-side
//permanent unique ID.
function getUniqueId() {
    var lastUniqueId = localStorage.getItem('lastUniqueId');
    //This evaluates to 1 when undefined
    lastUniqueId++;
    localStorage.setItem('lastUniqueId', lastUniqueId);
    return ":" + lastUniqueId;
}//getUniqueId


//Return the current user email, if logged in
function getCurrentUser() {
	return loggedInAs();
}//getCurrentUser



//Return the key to use for saving information to localstorage.
function getDataStorageField() {
	var user = getCurrentUser();
	//Can't save anything if we can't even figure out who the user is
	if (user === undefined) {
		return undef;
	}
    return LOCALSTORAGE_LISTS_KEY + ':' + user;
}//getDataField



//Save the updated data to local storage, and try to sync to the server unless dontSync is passed
//data can be passed, but defaults to gData[field] if undefined
function saveData(data, dontSync) {
	var storageField = getDataStorageField();
	if (storageField === undefined) {
		return;
	}
    if (data === undefined) {
        data = gData[LOCALSTORAGE_LISTS_KEY];
    }
    else {
        gData[LOCALSTORAGE_LISTS_KEY] = data;
    }
    localStorage.setItem(storageField, JSON.stringify(data));
    if (!dontSync) {
    	syncData();
    }
}//saveData


function loggedInAs() {
	return read_cookie("isLoggedIn");
}


//Loads the data structure from gData in memory, or from
//localStorage if gData isn't initialized yet.
//Returns undef if we don't know the user yet
function getData() {
    if (gData[LOCALSTORAGE_LISTS_KEY] === undefined) {
    	var storageField = getDataStorageField();
    	if (storageField === undefined) {
    		return undef;
    	}
    	
        var dataStr = localStorage.getItem(storageField);
        //console.log("getData: field = " + LOCALSTORAGE_LISTS_KEY + ", data_str = " + data_str);
        if (dataStr == undefined) {
        	var blank = {};
        	blank[USER_DATA] = {};
        	blank[USER_DATA][getCurrentUser()] = {};
        	dataStr = JSON.stringify(blank);
        }
        gData[LOCALSTORAGE_LISTS_KEY] = JSON.parse(dataStr);
        syncData();
    }
    return gData[LOCALSTORAGE_LISTS_KEY];
}//getData



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
	var sentData = getData();
	var sentLastUpdateDate = sentData["lastUpdate"];
	var params = {"content" : JSON.stringify(sentData)};
	$.ajax({
		type: "POST",
		url: LISTEY_HOME + "ajax",
		data: params,
		success: function(returnedData){
			//XXX - Need to rework this to compare client and server and
			//only refresh if change pushed from server and affects current screen.
			console.log("syncData call returned: " + returnedData);
			if (returnedData !== JSON.stringify(sentData)) {
console.log("Different! returnedData='"+returnedData+"', sentData='"+JSON.stringify(sentData)+"'");
				var returnedDataHash = JSON.parse(returnedData);
				console.log("Data changed, saving updated data");
				saveData(returnedDataHash, true);//true param means don't try to sync again
		
				//XXX NEED A WAY TO REFRESH CURRENT SCREEN IF CHANGED
				//just refresh everything.  Note, this could be nicer!
				//window.location = "index.html";
			}
		},
		statusCode: {
			403: function() {
				handleNotLoggedIn();
			}
		}
	});//ajax
}//syncData


//Return a list of user ids whose lists we can access, including current user
function getUsers() {
	var data = getData();
	return (keys(data[USER_DATA]));
}//getUsers



//Get the lists object for user
function getLists(user) {
	var data = getData();
	var userData = data[USER_DATA];
	if (!(user in userData)) {
		userData[user] = {};
	}
    if (!(LISTS in userData[user])) {
    	userData[user][LISTS] = {};
    }
    return userData[user][LISTS];
}//getLists



//Create a new, active list named newName
//Returns true if the new list was created, or false if a list with
//that name already existed.
//Since you can only add lists for your own user, there is no need
//to specify a user here
function addList(newName) {
    var didAdd = false;

    var user = getCurrentUser();
    var lists = getLists(user);
    for (var k in lists){
        if (lists.hasOwnProperty(k) && lists[k].name === newName) {
        	console.log("configureList: " + newName + " already exists, not adding again");
        	return false;//didn't add
        }
    }
    console.log("configureList: Saving new list " + newName);
    var newList = {};
    lists[getUniqueId()] = newList;
    newList[LAST_UPDATE] = now();
    newList[NAME] = newName;
    newList[STATUS] = ACTIVE_STATUS;

    saveData();
    
    return true;//did add
}//addList


//get the list object with id listId
//If no list is found with that id, then looks through all the lists
//to see if there is one with name matching listName, and returns that
function getList(user, listId, listName) {
	var lists = getLists(user);
	var list = lists[listId];
	if (list === undefined) {
		for (var k in lists) {
			if (lists.hasOwnProperty(k) && lists[k].name === listName) {
				return lists[k];
			}
		}//foreach key
	}//if !list
	
	return (list);
}//getList


//get a list of all the lists for the user sorted by name
function getListOfLists(user) {
    var lists = getLists(user);
    var listOfLists = [];
    for (var k in lists){
        if (lists.hasOwnProperty(k)) {
        	//have to make sure the unique id is set
        	//so caller can get it.
        	lists[k].uniqueId = k;
        	listOfLists.push(lists[k]);
        }
    }

    listOfLists.sort(compareHashesByName);

    return listOfLists;
}//getListOfLists


//Return the internal list of all items of all statuses
function getAllItems(user, listId, listName) {
	//Filter out ones matching the requested status and sort by name
    var list = getList(user, listId, listName);
    if (!(ITEMS in list)) {
    	list[ITEMS] = [];
    }
    return list[ITEMS];
}//getAllItems



//Return a list of items of the given status sorted by name, or ACTIVE_STATUS status if undefined
//This is NOT the internal list, so modifying the list will have no permanent effect
function getItems(user, listId, listName, status) {
	var items = getAllItems(user, listId, listName);
    if (status === undefined) {
    	status = ACTIVE_STATUS;
    }
    var rv = [];
    for (var i=0; i<items.length; i++) {
        if (items[i].status === ACTIVE_STATUS) {
        	rv.push(items[i]);
        }
    }
    
    rv.sort(compareHashesByName);
    return (rv);
}//getItems


//
function getItemIndex(user, listId, listName, itemId) {
	var items = getAllItems(user, listId, listName);
    for (var i = 0; i < items.length; i++) {
        if (items[i].uniqueId === itemId) {
            return i;
        }
    }
    return -1;
}//getItemIndex


/*
  (itemObj) getItem(user, listId, listName, itemId)

  itemsType is optional.  If not passed, will look in main
  list and crossed off list.
*/
function getItem(user, listId, listName, itemId) {
    var items = getAllItems(user, listId, listName);

    var itemIndex = getItemIndex(user, listId, listName, itemId);

    return (itemIndex === -1 ? undefined : items[itemIndex]);
}//getItem



/*
  () addOrUpdateItem(user, listId, listName, item)

  If itemId is defined in item, updates that item. Otherwise, creates new item.
*/
function addOrUpdateItem(user, listId, listName, item) {
    if (!(NAME in item)) {
        console.log("addItem: item parameter doesn't have a name field, skipping");
        return false;
    }

    item[LAST_UPDATE] = now();

    //get it in main list or crossed off list.  If not found in either
    //then add to main list
    var items = getAllItems(user, listId, listName);
    if (!(UNIQUE_ID in item)) {
    	item[UNIQUE_ID] = getUniqueId();
    	item[STATUS] = ACTIVE_STATUS;
        console.log("addItem: Saving new item " + item[NAME]);
        items.push(item);
    }//item doesn't exist
    else {
    	var itemIndex = getItemIndex(user, listId, listName, items, itemId);
        console.log("addItem: " + item[NAME] + " already exists, updating instead");
        items[itemIndex] = item;
    }//item already exists

    saveData();
}//addOrUpdateItem



//(removeItem) removeItem(user, listId, listName, itemId)
function removeItem(user, listId, listName, itemId) {
    var itemIndex = getItemIndex(user, listId, listName, itemId);

    var removedItem;
    if (itemIndex !== -1) {
        console.log("removeItem: removing " + itemId);

        var items = getAllItems(user, listId, listName);
        var itemsSpliced = items.splice(itemIndex, 1);
        removedItem = itemsSpliced[0];
        saveData();
    }//item doesn't exist

    return removedItem;
}//removeItem



//add a new category with the given name to the list
function addCategory(user, listId, listName, catName) {
	var list = getList(user, listId, listName);

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
			var newCat = {};
			newCat[LAST_UPDATE] = now();
			newCat[NAME] = newName;
			newCat[STATUS] = ACTIVE_STATUS;
			newCat[UNIQUE_ID] = getUniqueId();	            
			categories.push(newCat);
			categories.sort(compareHashesByName);
		}//category doesn't exist
		else {
			console.log("configureCategory: " + newName + " already exists, not adding again");
		}//item already exists

		saveData();
	}
	else {
		console.log("Current list " + listId + "(" + listName +") is somehow not defined!");
	}
}//addCategory



//Returns the array of categories for the given listId, listName
function getCategories(user, listId, listName) {
    var list = getList(user, listId, listName);
    var allCategories = list[CATEGORIES];
    var rv = [];
    if (allCategories !== undefined) {
    	for (var i=0; i<allCategories.length; i++){
    		if (allCategories[i].status === ACTIVE_STATUS) {
    			rv.push(allCategories[i]);
    		}
    	}
    }
    return (rv);
}//getCategories



//Returns a map whose keys are the category ids of the currently selected categories
function getSelectedCategories(user, listId, listName) {
    var list = getList(user, listId, listName);
    var currentFilterCategories = list[SELECTED_CATEGORIES];
    var rv = {};
    if (currentFilterCategories !== undefined) {
    	for (var i=0; i<currentFilterCategories.length; i++) {
    		rv[currentFilterCategories[i]] = true;
    	}
    }
    return rv;
}//getSelectedCategories



//Saves the updated selected categories set.  categoriesMap is a map whose keys are the
//selected category ids.
function saveSelectedCategories(user, listId, listName, newCategoriesMap) {
    var list = getList(user, listId, listName);
    
    list[SELECTED_CATEGORIES] = keys(newCategoriesMap);
    saveData();
}


//Reads the listId, listName from the url, puts a list of all possible
//categories in categoriesDivId, with the categories in selectedCategoriesHash
//checked
function displayCategories(categoriesDivId, selectedCategoriesHash) {
    console.log("displayCategories - top\n");

    var user = getUrlVars()[USER];
    var listId = getUrlVars()[LIST_ID];
    var listName = getUrlVars()[LIST_NAME];

    //Note, this assumes listName is a valid list
    var categories = getCategories(user, listId, listName),
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
            console.log("   Adding " + value[NAME]);
            var attributes = {"type":"checkbox", class:"custom", "id":"checkbox-"+index, "value":value[UNIQUE_ID]};
            if (selectedCategoriesHash[value[UNIQUE_ID]]) {
                attributes["checked"] = "true";
            }
            inputElem = $("<input>", attributes);
            fieldSetElem.append(inputElem);
            labelElem = $("<label>", {"for":"checkbox-"+index});
            labelElem.text(value[NAME]);
            fieldSetElem.append(labelElem);
        });//each item
        fieldContainElem.append(fieldSetElem);
        //replace the current lists div contents with the new unordered list
        $("#" + categoriesDivId).html(fieldContainElem);

        //have to explicitly transform to pretty view after initial page load
        $("#" + categoriesDivId).trigger('create');
    }//else not empty
}//displayCategories

