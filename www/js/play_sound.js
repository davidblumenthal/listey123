        <script type="text/javascript" charset="utf-8">

        // Wait for Cordova to load
        //
        document.addEventListener("deviceready", onDeviceReady, false);

        // Cordova is ready
        //
        var deviceReady = false;
        function onDeviceReady() {
	    debugOut("Device Is Ready!");
            deviceReady = true;
        }

        // Audio player
        //
    function playAudio(src) {
      debugOut("UserAgent = " + navigator.userAgent);
	  if((navigator.userAgent.match(/android/i))) {
	      //CODE_FOR_ANDROID

            src = getPhoneGapPath() + src;
    	    debugOut('playing ' + src);
            if (!deviceReady){
            	debugOut('not ready');
                return;
            }
            try {
              debugOut("Creating Media");
              var my_media = new Media(src, success, error_error);

              // Play audio
              debugOut("about to play with media");
              my_media.play();
            } catch (e) {
            	debugOut(e.Message);
            }
        }
        else {
	      //CODE_FOR_IPHONE_||_IPAD
  	      var source= document.createElement('source');
	      audio= document.createElement('audio');
	      source.type='audio/mp3';
	      source.src= src;

	      audio.setAttribute('src', source.src);
          // Play audio
          debugOut("about to play using audio tag");
          audio.play();
        }
    }

	function getPhoneGapPath() {
	    var path = window.location.pathname;
	    path = path.substr( path, path.length - 10 );
	    return 'file://' + path;
	};

</script>