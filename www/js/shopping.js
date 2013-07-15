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

var gSelectedList;

function sortItemsByName (a, b) {
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


function configureList() {
	console.log("configureList - top\n");
	var elem = $("#listName");
	if (elem === undefined) {
		alert("Can't find listName element");
		return;
	}
	var newName = trim(elem.val());

	if (newName.length == 0) {
		alert("You didn't enter a list name");
		return false;
	}

	console.log("configureList: newName = " + newName);
	var data = get_data();
	if (!(newName in data)) {
		console.log("configureList: Saving new list " + newName);
		data[newName] = {lastUpdate: now()
	        	        };
		save_data(data);
		displayLists();
	}
	else {
		console.log("configureList: " + newName + " already exists, not adding again");
	}
	//blank out value so it will be blank on next load
//	$("#listName").val("");

	//close the dialog
	$('.ui-dialog').dialog('close');
}





function configureItem() {
	console.log("configureItem - top\n");
	var elem = $("#itemName");
	if (elem.length == 0) {
		alert("Can't find itemName element");
		return;
	}
	var newName = trim(elem.val());

	if (newName.length == 0) {
		alert("You didn't enter an item name");
		return false;
	}

	console.log("configureItem: newName = " + newName);
	var data = get_data();
	var list = data[gSelectedList];
	if (list !== undefined)  {
		if (!("items" in list)) {
			console.log("adding items list to " + listName);
			list["items"] = [];
		}
		var items = list["items"];
		
		var index;
		for (var i = 0; i < items.length; i++) {
		   if (items[i].name === newName) {
		      index = i;
		      break;
		   } 
		}
		
		if (index === undefined) {
			console.log("configureItem: Saving new item " + newName);
			items.push({lastUpdate: now(),
				    name: newName});
			items.sort(sortItemsByName);
	        }//item doesn't exist
	        else {
	        	console.log("configureItem: " + newName + " already exists, not adding again");
	        }//item already exists
		save_data(data);
		displayItems();
	}
	else {
		console.log("Current list " + gSelectedList + " is somehow not defined!");
	}
	//blank out value so it will be blank on next load
//	$("#itemName").val("");

	//close the dialog
	$('.ui-dialog').dialog('close');
}


function save_data(data, field) {
	if (field === undefined) {
		field = 'lists';
	}
	localStorage.setItem(field, JSON.stringify(data));
}

function get_data(field) {
	if (field === undefined) {
		field = 'lists';
	}
	var data_str = localStorage.getItem(field);
console.log("get_data: field = " + field + ", data_str = " + data_str);
	if (data_str == undefined) {
		return {};
	}
	return JSON.parse(data_str);
}

function getUrlVars() {
    var vars = {}, keyval;
    var keyvals = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < keyvals.length; i++)
    {
        keyval = keyvals[i].split('=');
        vars[decodeURIComponent(keyval[0])] = decodeURIComponent(keyval[1]);
    }
    return vars;
}


$( document ).ready(function() {
	$("#saveAddList").click(configureList);
	$("#saveAddItem").click(configureItem);
});
