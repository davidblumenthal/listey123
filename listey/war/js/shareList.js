
function shareList() {
	var oneEmail, emailList=[];
    console.log("shareList - top\n");

    $(".shareEmailClass").each(function(){
    	oneEmail = trim($(this).val());
    	if (oneEmail.length > 0) {
    		console.log("Sharing with " + oneEmail);
    	    emailList.push(oneEmail);
    	}
    });//each
    
    if (emailList.length > 0) {
    	//send an AJAX request to the server to share with these
    }
    //close the dialog
    $('.ui-dialog').dialog('close');
}


$(document).on('click', '#saveShareList', function() {
    console.log("Clicked on saveShareList");
    shareList();
});


$(document).on('click', '#shareListAddAnotherEmailButton', function() {
    console.log("Clicked on shareListAddAnotherEmailButton");
    var lastInput = $("[name='shareEmailAddress']:last");
    if (lastInput.length == 0) {
    	console.log("unable to find last input");
    }else {console.log("found last input");}
    var clone = lastInput.clone(); 
    lastInput.parent().append(clone);
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
