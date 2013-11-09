
function shareList() {
    console.log("shareList - top\n");



    //close the dialog
    $('.ui-dialog').dialog('close');
}


$(document).on('click', '#saveShareList', function() {
    console.log("Clicked on saveShareList");
    shareList();
});


$(document).on('click', '#shareListAddAnotherEmailButton', function() {
    console.log("Clicked on shareListAddAnotherEmailButton");
    shareList();
});


$(document).on('submit', '#share-list-dialog-form', function(eventObject) {
    console.log("share-list-dialog-form submitted");
    shareList();
    eventObject.preventDefault();
    return false;
});

$(document).on('pagebeforeshow', '#share-list-dialog', function() {
    var listName = getUrlVars()["list"];

    console.log("share-list-dialog pagebeforeshow: listName=" + listName);

    $("#shareListDialogListNameSpan").text(escapeHTML(listName));
});
