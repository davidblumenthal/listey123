
function configureList() {
    console.log("configureList - top\n");

    var origListName = $("#origListName").val();

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

    if (origListName.length>0) {
        if (origListName === newName) {
            console.log("configureListName: name didn't change");
        }
        else {//name changed
            //XXX
            alert("Renaming lists (from " + origListName + " to " + newName + ") is not currently implemented");
            return;
        }
    }//if renaming

    else {//if adding new
        if (addList(newName)) {
            //refresh the list of lists
            displayLists();
        }
    }//adding new

    //close the dialog
    $('.ui-dialog').dialog('close');
}


$(document).on('click', '#saveConfigureList', function() {
    console.log("Clicked on saveConfigureList");
    configureList();
});


$(document).on('submit', '#config-list-dialog-form', function(eventObject) {
    console.log("config-list-dialog-form submitted");
    configureList();
    eventObject.preventDefault();
    return false;
});

$(document).on('pagebeforeshow', '#configure-list-dialog', function() {
    var listName = getUrlVars()["list"];

    console.log("configure-list-dialog pagebeforeshow: listName=" + listName);

    if (listName === undefined) {
        $("#deleteListButton").hide();
        $("#origListName").val("");
    }
    else {
        $("#configureListHeader").text("Rename List");
        $("#origListName").val(listName);
        $("#listName").val(listName);
        $("#deleteListButton").show();
        //Add list parameter to configureList link
        $('#deleteListButton').attr("href", 'deleteListConfirm.html?list=' + encodeURIComponent(listName));
    }
});

