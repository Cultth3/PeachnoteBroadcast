package com.videobroadcast.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.ObjectifyService;
import com.videobroadcast.shared.AuthData;

/**
 * Garbage collection for datastore 2
 * 
 * Job 2: This servlet runs in a cron job to free the datastore from references
 * to unavailable videos. Videos can be unavailable when they been deleted or
 * set to "private" in the users' Youtube accounts by the users. Users always
 * have the possibility to delete their videos and to change their videos'
 * privacyStatus, so we have to check from time to time if the datastore has
 * access to all videos.
 * 
 * @author Tom
 *
 */

@SuppressWarnings("serial")
public class VideoBroadcastScheduledTaskServlet2 extends HttpServlet {
	
	private final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private final static JacksonFactory JSON_FACTORY = new JacksonFactory();
	private static final String APPLICATION_NAME = "Music Live Broadcasting";
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	private static YouTube youtube;
	
	private static final Logger log = Logger
			.getLogger(VideoBroadcastScheduledTaskServlet2.class.getName());
	
	static {
        ObjectifyService.register(BroadcastEntity.class);
    }
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("Cron Job 2");

		try {

			log.info("Cron Job 2 \"Delete_references_to_unavailable_videos\" is running");

			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName(APPLICATION_NAME).build();
			
			
//			List<BroadcastEntity> broadcastList = ofy().load().type(BroadcastEntity.class).list();
			
			Query q = new Query("BroadcastEntity");
			PreparedQuery pq = datastore.prepare(q);
			

			// First cast List<BroadcastEntity> to List<String> and put all ids in deletionList
			List<String> deletionList = new ArrayList<>();
//			Iterator<BroadcastEntity> iterator = broadcastList.iterator();
			
			for (Entity entity : pq.asIterable()) {
				deletionList.add(entity.getKey().getName());
			}
			
			YouTube.Videos.List videoList = youtube.videos().list("id");
			// It's important to put ALL ids in the videoList. Missing entries will be deleted.
			videoList.setId(deletionList.toString().substring(1, deletionList.toString().length() - 1)); 
			videoList.setKey(AuthData.API_KEY); // For unauthorized API calls OAuth 2.0 is not necessary
			VideoListResponse response = videoList.execute();
//			System.out.println(response.toPrettyString());
			
			// Check which videos still exist on Youtube and put their ids in existingVideoList
			List<String> existingVideosList = new ArrayList<>();
			for (int i=0; i < response.getItems().size(); i++) {
				existingVideosList.add(response.getItems().get(i).getId()); // Only existing videos have an id in the items field
//				System.out.println("Existing video " + i + ": " + response.getItems().get(i).getId());
			}
			
			log.info("Datastore before removing non-existing videos: " + deletionList.toString());
			deletionList.removeAll(existingVideosList); // Delete all ids of existing videos from deletionList
			
			log.info("Existing IMSLP videos on Youtube: " + existingVideosList.toString());
			log.info("Unavailable broadcasts to delete: " + deletionList.toString());

			try {
				if (!deletionList.isEmpty()) {
					
//						ofy().delete().type(BroadcastEntity.class).id(id);
//						BroadcastEntity broadcastEntity = ofy().load().type(BroadcastEntity.class).id(id).now();
//						ofy().delete().entity(broadcastEntity).now();

					for (Entity entity : pq.asIterable()) {
						String key = entity.getKey().getName();
						Iterator<String> iterator2 = deletionList.iterator();
						
						while (iterator2.hasNext()) {
							if (iterator2.next().equals(key)) {
								ofy().delete().entity(entity.getKey()).now();
								log.warning("Broadcast " + key + " was unavailable and has been removed from datastore!");
							}
						}
						
					}
					
					log.info("Entries in database after deletion: " + ofy().load().type(BroadcastEntity.class).list().size());
				}
				else 
					log.info("Cron Job 2: No entries to delete");
				
				log.info("Cron Job 2 \"Delete_references_to_unavailable_videos\" has been executed");
			
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.warning("Cron job 2 failed!");
			log.warning("Reason: " + ex.getMessage());
		}
	}
	
}
