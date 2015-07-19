package com.videobroadcast.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.googlecode.objectify.ObjectifyService;

/**
 * 
 * Garbage collection for datastore 1
 * 
 * Job 1.1: This servlet runs in a cron job to delete video references on
 * IMSLP/in the datastore to videos the user didn't decide what to do with.
 * After finishing the livestream (lifeCycleStatus = "complete") the user is
 * prompted to decide whether to keep or delete the video. If the user doesn't
 * decide at all this cronjob will delete the reference to this video after
 * VideoBroadcastServiceImpl.MAX_TIME_IN_DATASTORE. After this task 
 * the video of which reference has been deleted can't be watched on IMSLP
 * and in the TV section anymore, but it still exists in the user's Youtube account.
 * 
 * Job 1.2: This servlet also deletes references to broadcasts which are live
 * for more than VideoBroadcastServiceImpl.MAX_TIME_LIVE. We assume that after
 * this time the broadcast accidently hasn't been terminated properly (user
 * refreshed page) so the reference to the broadcast/video must be deleted. This
 * eliminates long videos on IMSLP that contain mostly nonsensical content.
 * 
 * @author Tom
 *
 */

@SuppressWarnings("serial")
public class VideoBroadcastScheduledTaskServlet extends HttpServlet {
	// implements VideoBroadcastServiceScheduledTaskService
	
	private static final Logger log = Logger
			.getLogger(VideoBroadcastScheduledTaskServlet.class.getName());
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	static {
        ObjectifyService.register(BroadcastEntity.class);
    }

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("Cron Job 1");

		log.info("Cron Job 1 \"Delete_old_references_and_too-long-living_live_broadcasts\" is running");

		try {


			// Job 1.1: Get and delete all broadcasts where stayOnIMSLP = false 
			// and endTime (= Start time + VideoBroadcastServiceImpl.MAX_TIME_IN_DATASTORE) < now 
			// and lifeCycleStatus = "complete" 
			// This is the case if a user stops streaming but doesn't tell us in the dialog (stopStreamingOverlay) what to do with the video 
			Filter notInStandardListFilter = new FilterPredicate("stayOnIMSLP", FilterOperator.EQUAL, false);
			Filter endTimeFilter = new FilterPredicate("deletionTime",
					                      FilterOperator.LESS_THAN,System.currentTimeMillis());
			Filter statusFilter = new FilterPredicate("lifeCycleStatus", FilterOperator.EQUAL, "complete");
			
			Filter timeAndStandardListFilter =
					  CompositeFilterOperator.and(statusFilter, notInStandardListFilter, endTimeFilter);
			
			Query q = new Query("BroadcastEntity").setFilter(timeAndStandardListFilter);
			PreparedQuery pq = datastore.prepare(q);
			
			for (Entity result : pq.asIterable()) {
//			    System.out.println(result.toString());
				ofy().delete().entity(result.getKey());
			    log.warning("Broadcast " + result.getKey()
						+ " has been deleted from datastore (Job 1.1: MAX_TIME_IN_DATASTORE)!");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.warning("Cronjob 1.1 failed!");
			log.warning("Reason: " + ex.getMessage());
		}
			
			// Job 1.2: Delete all broadcasts where now >= ScheduledStartTime + MAX_TIME_LIVE and
			// lifeCycleStatus = live (Then we assume that the broadcast hasn't been terminated properly)
		
		try {
			
			Filter statusFilter2 = new FilterPredicate("lifeCycleStatus", FilterOperator.EQUAL, "live");
			Filter liveEndTimeFilter = new FilterPredicate("liveEndTime",
                    FilterOperator.LESS_THAN,System.currentTimeMillis());
			
			Filter statusAndLiveEndTimeFilter =
					  CompositeFilterOperator.and(statusFilter2, liveEndTimeFilter);
			
			Query q2 = new Query("BroadcastEntity").setFilter(statusAndLiveEndTimeFilter);
			PreparedQuery pq2 = datastore.prepare(q2);
			
			for (Entity result : pq2.asIterable()) {
//			    System.out.println(result.getKey());
				ofy().delete().entity(result.getKey());
			    log.warning("Broadcast " + result.getKey()
						+ " has been removed from datastore (Job 1.2: MAX_TIME_LIVE)!");
			}
			
			log.info("Cron Job 1 \"Delete_old_references_and_too-long-living_live_broadcasts\" has been executed");

		} catch (Exception ex) {
			ex.printStackTrace();
			log.warning("Cronjob 1.2 failed!");
			log.warning("Reason: " + ex.getMessage());
		}
	}
	
}
