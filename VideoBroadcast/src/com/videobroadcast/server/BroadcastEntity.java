package com.videobroadcast.server;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class BroadcastEntity implements Comparable<BroadcastEntity> {

	@Id String id;
	@Index long deletionTime;	// At this time the broadcast will be deleted if (boolean stayOnIMSLP == false) 
	@Index long liveEndTime;	// If at this time the broadcast is still live Cron Job 1 will delete it (MAX_TIME_LIVE exceeded)
	@Index long startTime;		// Time in millis when user goes live
	@Index long endTime;		// Time in millis when broadcast stopped
	@Index boolean stayOnIMSLP = false;
	@Index String lifeCycleStatus = "live"; // Only broadcasts with status "live" will be stored in the database
	@Index String piece;		// Title of the IMSLP piece (Important: Title equals exactly the name of the piece on IMSLP!)
	@Index String channelId;    // Id of the video owner's Youtube channel
	@Index String privacyStatus; // The video's privacy status when broadcast is created. This value won't be updated if the user changes 
							     // video's privacy settings in his Youtube account
	@Index String channelName;  // Youtube channel name
	String date;				// Date in Youtube's ISO ... format
	
	
	@Override
	public int compareTo(BroadcastEntity o) {
		if (this.startTime < o.startTime)
			return -1;
		else if (this.startTime == o.startTime)
			return 0;
		else
			return 1;
	}

	
	
}
