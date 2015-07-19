/* This Youtube function has to be global */
function onYouTubeIframeAPIReady() {
	createAndAddVideoList();
}

function handleThumbnailClick(elementIndex) {
	videoDivs[elementIndex].find('div').innerHTML = "";
	createYoutubePlayer(videoDivs[elementIndex]);
};

(function($){
		
	var base = '//1-dot-peachnote-broadcast.appspot.com';  
//    var base = '//localhost:8888';
	
	var landingPageUrl = base + "/Landingpage/LandingpageA/preview.html";
	
	if (!window.localStorage || !window.localStorage.getItem || !window.localStorage.setItem) {
        return;
    }
    var rand = window.localStorage.getItem("mlb-rand");
    if (rand === null) {
        rand = Math.random();
        window.localStorage.setItem("mlb-rand", rand);
    }
    
//    if (rand < 0.5) {
//    	landingPageUrl = base + "/Landingpage/LandingpageB/preview.html"; // Landingpage for the survey
//    } 

    if (!window.wgCategories || window.wgCategories.indexOf("Scores") == -1) {
        /* wrong page category :) */ 
        return;
    }

    
    const playerWidth = '256';
    const playerHeight = '144';
    const thumbnailPadding = '-24'; // Exactly like Youtube does in video lists, we only show the 16:9 region of the thumbnails 
    								// (thumbnails are all in 4:3 format). For this we need a padding to hide the "blacked out" part 
    								// of the thumbnails depending on the thumbnail size.

    var results = [];
    window.videoDivs = [];
    var players = [];
    var videoListContent;	
    
    $('<style>.video-entry:nth-child(1){margin-left: 0;border-left:none;width:128px;}' + 
    '.cf:before,.cf:after{content: " ";display:table;}.cf:after{clear:both;}' +
    '.text-muted{color: #777;}' +
    '.video-entry {display: inline-block;white-space: normal;margin-left: 0.5em;margin-right: 0.5em;' +
    'border-left: 1px solid #bbb;padding-left: 0.8em;padding-right: 0.8em; max-width: 246px;}' +
    '.videoDiv {width: ' + playerWidth + 'px; height: ' + playerHeight + 'px; border: 2px solid #ebeae4;}' +
    '.videoDivLive {width: ' + playerWidth + 'px; height: ' + playerHeight + 'px; border: 2px solid red;}' +
    '.videoInfo {padding: 4px; width: 252px; } ' +  // playerWidth - 4px
    '.videoThumbnailWrapper {width: ' + playerWidth + 'px; height: ' + playerHeight + 'px; overflow: hidden;} </style>' +
    '.mlbHead{background: #c2c8cd; padding: 5px 10px 5px 3.5em; border-bottom: 1px solid #788791;}').appendTo($('head'));

    var formatTime = function (sec_num) {
        var hours   = Math.floor(sec_num / 3600);
        var minutes = Math.floor((sec_num - (hours * 3600)) / 60);
        var seconds = sec_num - (hours * 3600) - (minutes * 60);
        seconds = Math.round(seconds);
        if (hours   < 10) {hours   = "0"+hours;}
        if (minutes < 10) {minutes = "0"+minutes;}
        if (seconds < 10) {seconds = "0"+seconds;}
        var time    = hours+':'+minutes+':'+seconds;
        return time;
    };
    
    var formatDate = function(d) {
        var date = new Date(d), now = Date.now();
        var diffMs = now - d;
        var diffS = diffMs/1000;
        var diffMn = diffS/60;
        var diffHs = diffMn/60;
        var diffDs = diffHs/24;
        if (diffDs > 7) {
            return date.toLocaleDateString();
        }
        if (diffHs > 22) {
            var r = Math.round(diffDs);
            return r + " day"+(r > 1 ? "s":"")+" ago";
        }
        if (diffMn > 50) {
            var r = Math.round(diffHs);
            return r + " hour"+(r > 1 ? "s":"")+" ago";
        }
        if (diffS > 50) {
            var r = Math.round(diffMn);
            return r + " minute"+(r > 1 ? "s":"")+" ago";
        }
        if (diffS > 10) {
            var r = Math.round(diffS);
            return r + " second"+(r > 1 ? "s":"")+" ago";
        }
        return "just now";
    };

    
    window.createYoutubePlayer = function(videoDiv) {
    	var player = new YT.Player(videoDiv.find('div').attr("id"), {
    	    height: playerHeight,
    	    width: playerWidth,
    	    videoId: videoDiv.find('div').attr("videoId"),
    	    playerVars: {
    	    	'controls':2,
    	    	'iv_load_policy':3,
    	    	'modestbranding':1,
    	    	'rel':0,
    	    	'showinfo':0,
    	    	'theme':"light"
    	    },
    	    events: {
    	      'onReady': onPlayerReady,
    	      'onStateChange': onPlayerStateChange
    	    }
    	  });
    	
    	players[players.length] = player;
    	
    	// Consider: The "Permission denied to access property 'toString'" error and other errors (depending on the browser) 
    	// seem to be common video embedding iframe errors and can only be fixed by Adobe's Flash Player team 
    	// or by the Google engineers, so everyone ignores them.
    	
    };

    // The API will call this function when the video player is ready.
    var onPlayerReady = function(event) {
    	for (var i=0; i < players.length - 1; i++) {
    		players[i].stopVideo();
    	}
    	event.target.playVideo();
    };

    var onPlayerStateChange = function(event) {
    	//  if (event.data == YT.PlayerState.PLAYING) {}
    };

    
    window.createAndAddVideoList = function() {

		/* Add content of the server's response */
		
		var videoCount = 0;
		var inner = $('#inner');
		var list = $('#list');
	     
		for (var i = 0; i < results.length; i++) {
	        var r = results[i];
	        videoCount++;
	        
	        startTime = r.startTime;
	        endTime = r.endTime;
	        channelName = r.channelName;
	        var time = formatTime((endTime - startTime)/1000); // s.stopTime - s.startTime
	        var date = formatDate(startTime);
	        
	        var videoEntry = $('<div class="video-entry">');
	        var thumbnail = $('<div class="videoThumbnailWrapper">' + 
	        		'<img src="//img.youtube.com/vi/' + r.id + '/0.jpg" onclick="handleThumbnailClick(' + i + ')"  aria-hidden="true"' +  
	        		'style="cursor:pointer; margin-top: '+ thumbnailPadding + 'px; width: 100%; "></img> </div>');
	        
	        var videoDiv;
	        var videoInfoArea = $('<div class="videoInfo">');
	        var inner = $('<div>').appendTo(videoEntry);

	        if (r.lifeCycleStatus == "live") {
	        	videoDiv = $('<div class="videoDivLive"> <div id="videoDiv'+i+'" videoId="'+ r.id + '"' + 
	        			'style="width: ' + playerWidth + 'px;"> </div> </div>');
	        	videoDiv.find('div').append(thumbnail);
	        	inner.append(videoDiv);
	        	$('<span style="float:right" class="ddm">').append('<span style="color: red; margin-right: 0.1em;">' + 'live!' + '<span>')
	        																									.appendTo(videoInfoArea);
	        }
	        else if (r.lifeCycleStatus == "complete") {
	        	videoDiv = $('<div class="videoDiv"> <div id="videoDiv'+i+'" videoId="'+ r.id + '" style="width: ' + playerWidth + 'px;"> </div> </div>');
	        	videoDiv.find('div').append(thumbnail);
	        	inner.append(videoDiv);
	        	if (endTime > 0)
	        		$('<span style="float:right" class="ddm">').append('<span class="icon icon47"></span>' + time).appendTo(videoInfoArea);
	        }
	        
	        videoInfoArea.appendTo(inner);
	        
	        videoDivs[i] = videoDiv;
//	        thumbnail.click(function() {handleThumbnailClick(videoDivs[i]);});
	        list.append(videoEntry);
	        	
	        /* Add all video information */
	        $('<span class="ddm">').text(date).prepend('<span class="icon icon33"></span>').appendTo(videoInfoArea);
	        $('<br>').appendTo(videoInfoArea);
	        $('<span class="text-muted"> by </span>').appendTo(videoInfoArea);
	        $('<span style="max-width: 100px;">').text(r.channelName).appendTo(videoInfoArea);
//	        $('<span style="float:right;" class="ddm">').append('<span class="icon icon100"></span>'+ r.dislikeCount).appendTo(videoInfoArea);
	        $('<span style="float:right;" class="ddm">').append('<span class="icon icon84"></span>' + r.viewCount).appendTo(videoInfoArea);
	        if (r.likeCount != 0) { // Is 0 if the owner of the video doesn't allow others to see the video ratings
	        	$('<span style="float:right;" class="ddm">').append('<span class="icon icon101"></span>'+ r.likeCount).appendTo(videoInfoArea);
	        }
	        $('<br>').appendTo(videoInfoArea);

		}
	        
		var tabTitle = 'Live video ('+(videoCount)+')';
		$('a[href=#tab-musiclivebroadcasting]').text(tabTitle);
    	
    };

    
    var invokeVideoListCreation = function(data) {
    	if (!data.results) {
    		console.error('no results in '+data);
    		return;
    	}
    	
    	results = data.results;
    	
    	if (results.length > 0) {
    		/* Call Youtube iframe API only if necessary */
    		var tag = document.createElement('script');
    		tag.src = "https://www.youtube.com/iframe_api";
    		var firstScriptTag = document.getElementsByTagName('script')[0];
    		firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
    		
    		// Important: The global function onYouTubeIframeAPIReady() will be called
    		
        }
    	
    };
    

    var process = function(title) {

    	videoListContent = $('<div class="we" style="max-width:initial">');
        var head = $('<div style="background: #c2c8cd; padding: 5px 10px 5px 3.5em; border-bottom: 1px solid #788791; padding-left: 1em;">'+ // class="mlbHead" bzw. oly-head
        '<div class="we_file_download"><p><b>Live video recordings</b><br>'+
        '<span class="we_file_info2">You can find recordings of this piece played by other users here &mdash;'+
        ' Or record your own! <a href="' + landingPageUrl + '" target="_blank">Learn more...</a>' +
//        ' </br> You also can watch live video recordings of other IMSLP pieces in the <a href="' + base + '?section=tvsection" target="_blank"> TV section</a>. </span>'+
        '</p><div class="we_clear"></div></div><div class="we_clear">' + 
        '</div></div>');
        head.appendTo(videoListContent);

        var list = $('<div id="list" style=" /*overflow-x:auto;*/ width: 100%;display: table;white-space: nowrap;" class="cf">');
        var inner = $('<div id="inner" class="we_edition_info_i gainlayout"' + 
        		'style="margin-right: 0px !important; overflow-x:auto;"></div>').append(list);
        inner = $('<div class="we_edition_info gainlayout"></div>').append(inner);
        videoListContent.append(inner);
        
        var addOwn = $('<div class="video-entry" style="display:table-cell; vertical-align:middle;">');
        list.append(addOwn);
        $('<a class="nxbutton" style="font-size: 0.95em; min-width: 110px; float: left"'+
            ' target="_blank" href="'+landingPageUrl+ "?title=" + title +
            '"><span style="font-size: 1.7em;float:left;color: #a22;margin-top:-0.1em; line-height: 0.9em;">◉</span>'+
            '<span class="label blue">&nbsp;Record now</span></a>'
            ).appendTo(addOwn);
        

        /* Currently each IMSLP page has a "Performances" area, so we don't need to create it. Thanks. ;) */
        
        var tabRecordings = $('.tabs #Recordings');   
        var tabs_el = tabRecordings.parent().parent().parent();
        
        var tabs = tabs_el.tabs();
        var ul = tabs_el.find('ul');

        a = $('<a href="#tab-musiclivebroadcasting">').append('Live video (+)'); // provisional text that will be changed after the server's response.
        var b = $('<b>').append(a);
        var li = $('<li class="ui-state-default ui-corner-top">').append(b);
        li.appendTo(ul);
        var contentDiv = $('<div id="tab-musiclivebroadcasting">').append(videoListContent);
        contentDiv.appendTo( tabs );
        tabs.tabs('destroy');
        tabs.tabs();
        	
        /* Try to get video list contents from the server and if so then add it */
        
        var encodedTitle = encodeURI(title); 
        
        
        $.ajax({ 
            url: base+"/imslplistservlet?title=" + encodedTitle,  // + "&callback=createAndAddVideoList", 
            type: 'get',  
            dataType: 'jsonp',
            success: function( response ) {
            	invokeVideoListCreation(response);
            }
          }
    	);
		
    };

    // The end is the beginning
    var title = mw.config.get('wgTitle');
    process(title);
	
})(jQuery);