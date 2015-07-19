package com.videobroadcast.shared;

public class MyConstants {
	
	/*
	 * The minimal time a broadcast must be live. If the user stops streaming before
	 * MIN_STREAM_TIME the broadcast will be deleted.
	 */
	public final static int MIN_STREAM_TIME = 60000;  
//	public final static int MIN_STREAM_TIME = 1000; // test

	
	/*
	 * The time a broadcast persists in the datastore if the user doesn't 
	 * tell us what to do with the video (stayOnIMSLP or delete it) after stopping streaming.
	 * So it's the minimum time (+ the cron job schedule time) the user has for the choice if he wants
	 * to keep the video or to delete it. 
	 */
//	public static final long MAX_TIME_IN_DATASTORE = 3600000;
	public static final long MAX_TIME_IN_DATASTORE = 6000000;
	

	/*
	 * Max time that a broadcast can be live. If a broadcast is longer live we
	 * assume that it accidently hasn't been stopped correctly and delete it from IMSLP
	 */
	public static final long MAX_TIME_LIVE = 3600000;


	/*
	 * The limit of results the server searches for when the user types in the GoLiveOverlay's suggestBox.
	 */
	public static final int WIKI_PAGES_RESULT_LIMIT = 20;

}
