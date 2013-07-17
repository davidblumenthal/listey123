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
    var keyvals = window.location.href.slice(window.location.href.indexOf('?') + 1).split(/[&#]/);
    for(var i = 0; i < keyvals.length; i++)
    {
        keyval = keyvals[i].split('=');
        vars[decodeURIComponent(keyval[0])] = decodeURIComponent(keyval[1]);
    }
    return vars;
}
