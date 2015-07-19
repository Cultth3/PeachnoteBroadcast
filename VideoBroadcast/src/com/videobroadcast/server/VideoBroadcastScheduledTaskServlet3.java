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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.ObjectifyService;
import com.videobroadcast.shared.MyConstants;

/**
 * Garbage collection for datastore 3
 * 
 * Job 3: This servlet runs in a cron job to delete broadcasts that should be
 * deleted immediately by the client after streaming if the user didn't stream
 * long enough (shorter than MIN_TIME_LIVE). Normally this garbage collection
 * servlet should never be necessary because the deletion after streaming is
 * reliable. However if the deletion fails due to incomprehensible reasons this
 * servlet additionally helps to keep the datastore clean.
 * 
 * @author Tom
 *
 */

@SuppressWarnings("serial")
public class VideoBroadcastScheduledTaskServlet3 extends HttpServlet {
	// implements VideoBroadcastServiceScheduledTaskService
	
	private static final Logger log = Logger
			.getLogger(VideoBroadcastScheduledTaskServlet3.class.getName());
	
	static {
        ObjectifyService.register(BroadcastEntity.class);
    }
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("Cron Job 3");

		try {

			log.info("Cron Job 3 \"Delete_too_short-time_broadcasts\" is running");

//			List<BroadcastEntity> broadcastList = ofy().load().type(BroadcastEntity.class).list();
			
			Query q = new Query("BroadcastEntity");
			PreparedQuery pq = datastore.prepare(q);
			
			for (Entity entity : pq.asIterable()) {
				Key key = entity.getKey();
				BroadcastEntity x = ofy().load().type(BroadcastEntity.class).id(key.getName()).now();
				if (x.lifeCycleStatus.equals("complete") && x.endTime != 0 && x.endTime - x.startTime < MyConstants.MIN_STREAM_TIME) {
					// Delete Broadcast from datastore
					ofy().delete().entity(key);
//					ofy().delete().type(BroadcastEntity.class).id(id);
					log.warning("Broadcast " + key.getName() + " is too short has been removed from datastore!");
//					System.out.println("Broadcast " + key.getName() + " is too short has been removed from datastore!");
				}
			}	
			
			log.info("Cron Job 3 \"Delete_too_short-time_broadcasts\" has been executed");
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.warning("Cron job 3 failed!");
			log.warning("Reason: " + ex.getMessage());
		}
	}
	
}
