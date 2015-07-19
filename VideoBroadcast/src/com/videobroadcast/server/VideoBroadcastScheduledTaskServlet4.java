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
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.googlecode.objectify.ObjectifyService;
import com.videobroadcast.shared.AuthData;

/**
 * 
 * @deprecated
 * This garbage collection servlet is replaced by VideoBroadcastScheduledTaskServlet2
 * 
 * Garbage collection for datastore 4
 * 
 * Job 4: This servlet runs in a cron job to free the datastore from
 * references to videos of which the privacy status has been changed in the user's
 * Youtube account to "private". Users always have the possibility to change 
 * the privacy status of their videos. If the status is "private" only logged in users, 
 * that have permission to access the video, can watch it. 
 * So we have to check from time to time if all videos in the datastore
 * have still either the status "unlisted" or "public".
 * If the status is "private" the video will be deleted.
 * 
 * 
 * @author Tom
 *
 */

@SuppressWarnings("serial")
public class VideoBroadcastScheduledTaskServlet4 extends HttpServlet {
	
	private final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private final static JacksonFactory JSON_FACTORY = new JacksonFactory();
	private static final String APPLICATION_NAME = "Music Live Broadcasting";
	
	private static YouTube youtube;
	
	private static final Logger log = Logger
			.getLogger(VideoBroadcastScheduledTaskServlet4.class.getName());
	
	static {
        ObjectifyService.register(BroadcastEntity.class);
    }
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("Cron Job 4");

		try {

			log.info("Cron Job 4 \"Delete_references_to_unreachable_videos\" is running");

			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName(APPLICATION_NAME).build();
			
			List<BroadcastEntity> broadcastList = ofy().load().type(BroadcastEntity.class).list();

			
			YouTube.Videos.List videoList = youtube.videos().list("status");
			videoList.setKey(AuthData.API_KEY); // For unauthorized API calls OAuth 2.0 is not necessary
			
			List<String> stringIdList = new ArrayList<String>();
			
			Iterator<BroadcastEntity> iterator = broadcastList.iterator();
			while (iterator.hasNext()) {
				BroadcastEntity entity = iterator.next();
				stringIdList.add(entity.id);
			}
			videoList.setId(stringIdList.toString().substring(1, stringIdList.toString().length() - 1));
			VideoListResponse response = videoList.execute();

			System.out.println(response.getItems().toString());

			/* Put "private" videos in deletionList */
			List<String> deletionList = new ArrayList<>();
			List<Video> items = response.getItems();
			for (int i=0; i < response.getItems().size(); i++) {
				Video video = items.get(i);
				if(video.getStatus().getPrivacyStatus().equals("private")) {
					deletionList.add(video.getId());
				}
			}
			
			log.info("Database entities to delete: " + deletionList.toString());

			try {
				if (!deletionList.isEmpty()) {
					ofy().delete().type(BroadcastEntity.class).ids(deletionList.toString().substring(1, deletionList.toString().length() - 1));
					
					log.info("Entries in database after deletion: " + ofy().load().type(BroadcastEntity.class).list().size());
				}
				else 
					log.info("Cron Job 4: No entries to delete");
				
				log.info("Cron Job 4 \"Delete_references_to_unreachable_videos\" has been executed");
			
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.warning("Cron job 4 failed!");
			log.warning("Reason: " + ex.getMessage());
		}
	}
	
}
