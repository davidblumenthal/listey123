/*
  Data format:
    listName => {items : [{name : "Milk",
  	                       lastUpdate: "06/28/2013 13:25:29 CDT",
  	                      },
  	                      {name : "Eggs",
  	                       state: "deleted",
  	                       lastUpdate: "06/28/2013 13:25:29 CDT",
  	                      },
  	                      {name : "Doritos",
  	                       state: "purged",
  	                       lastUpdate: "06/28/2013 13:25:29 CDT",
  	                      },
  	                      ],
  	             crossedOffItems : [{name : "Butter",
  	                                 count: 2,
  	                       	         lastUpdate: "06/28/2013 13:25:29 CDT",
  	                                },
  	                               ],
  	             deletedItems : [{name : "Butter",
  	                              count: 2,
  	                              lastUpdate: "06/28/2013 13:25:29 CDT",
  	                             },
  	                            ],
  	             purgedItems : [{name : "Butter",
  	                             count: 2,
  	                             lastUpdate: "06/28/2013 13:25:29 CDT",
  	                            },
  	                           ],

  	             lastUpdate: "06/28/2013 13:25:29 CDT"
  	            }

*/

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
	var elem = $("#listName");
	if (elem === undefined) {
		alert("Can't find listName element");
		return;
	}
	var newName = trim($("#listName").val());

	if (newName.length == 0) {
		alert("You didn't enter a list name");
		return false;
	}

	console.log("configureList: newName = " + newName);
	var data = get_data();
	if (!(newName in data)) {
		console.log("configureList: Saving new list " + newName);
		data[newName] = {items:{},
	        	         lastUpdate: now()
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

function displayLists() {
	var data = get_data(),
		listNames = keys(data).sort(),
		ul;

	if (listNames.length == 0) {
		$("#lists").html("Click 'Add A List' to create a list");
	}
	else {
		ul = $("<ul>", {"data-role":"listview", "data-count-theme":"c", "data-inset":"true"});

		$.each(listNames, function (index, value) {
			ul.append("<li><a href='#list-page'>" + escapeHTML(value) + "<span class='ui-li-count'>" + keys(data[value]["items"]).length + "</span></a></li>");
		});

		//replace the current lists div contents with the new unordered list
		$("#lists").html(ul);

		//have to explicitly transform to listview after initial page load
		ul.listview();
	}
}//displayLists


function save() {
	var keyId="lang1", valId = "lang2";
	console.log("save clicked");
	var k = document.getElementById(keyId).value;
	var v = document.getElementById(valId).value;
	var data = get_data();
	data[k] = v;
	save_data(JSON.stringify(data));
	display('Saved ' + k +  ' ' + v);

	document.getElementById(keyId).value = '';
	document.getElementById(valId).value = '';

	list();

	return false;
}

function display(html,divId) {
	if (!divId) {
		divId = 'response';
	}
	document.getElementById(divId).innerHTML = html;
}

function list() {
	var data = get_data();
	var has_keys=0;
	for (var k in data) {has_keys++; break}

	if (has_keys) {
		var html = '<b>Listing:</b><br>';
		for (k in data){
			html += k + ' = ' + data[k] + '<br>';
		}
		display(html, 'list');
		return false;
	}
	display ('Empty', 'list');
	return false;
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

$( document ).ready(function() {
	displayLists();
	$("#saveAddList").click(configureList);
});
