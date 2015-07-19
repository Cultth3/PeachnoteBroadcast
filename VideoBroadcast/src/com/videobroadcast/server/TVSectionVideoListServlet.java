package com.videobroadcast.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.googlecode.objectify.ObjectifyService;
import com.videobroadcast.shared.AuthData;

/**
 * @deprecated 
 * @author Tom
 *
 */

@SuppressWarnings("serial")
public class TVSectionVideoListServlet extends HttpServlet {
	
	private final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private final static JacksonFactory JSON_FACTORY = new JacksonFactory();
	private static final String APPLICATION_NAME = "Music Live Broadcasting";
	
	private static YouTube youtube;
	
	private static final Logger log = Logger
			.getLogger(TVSectionVideoListServlet.class.getName());
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	static {
        ObjectifyService.register(BroadcastEntity.class);
    }

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("Get broadcast list");

		log.info("TVSectionVideoListServlet is running");

		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName(APPLICATION_NAME).build();
		
		resp.addHeader("Access-Control-Allow-Origin", "*");
		
		try {
			
			String jsonData = getDataAsJson(req.getParameter("search"));
			String output = req.getParameter("callback") + "(" + jsonData + ");";
	
			resp.setContentType("text/javascript");
//			resp.setContentType("application/json");
		          
			PrintWriter out = resp.getWriter();
//			out.println(output.substring(5, output.length()-2));
			out.print(req.getParameter("search"));
			System.out.println(output);
			
			log.info("TVSectionVideoListServlet has been executed");

		} catch (Exception ex) {
			ex.printStackTrace();
			log.warning("Get Broadcast list of TVSectionVideoListServlet failed!");
			log.warning("Reason: " + ex.getMessage());
		}
	}
	
	
	private String getDataAsJson(String searchTerm) {
		
		List<BroadcastEntity> resultList = searchInDatabase(searchTerm); 
		List<BroadcastEntity> sortedList = sortResultList(resultList);
		
		JSONArray jsonArray = new JSONArray();
		
		System.out.println("Create JSONObject and sort list");
		
		/* Create JSONObject */
		try {
			YouTube.Videos.List videoList = youtube.videos().list("status, statistics");
			videoList.setKey(AuthData.API_KEY); // For unauthorized API calls OAuth 2.0 is not necessary
			
			for (BroadcastEntity entity:sortedList) {
				
				System.out.println("New video entry");
				
				String id = entity.id;

				videoList.setId(id);
				VideoListResponse response = videoList.execute();
				System.out.println(response);
				try {
					VideoStatistics statistics = response.getItems().get(0).getStatistics();
					
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("id", id);
					jsonObj.put("channelName", entity.channelName);
					jsonObj.put("piece", entity.piece);
//					jsonObj.put("startTime", entity.startTime);
//					jsonObj.put("endTime", entity.endTime);
					jsonObj.put("lifeCycleStatus", entity.lifeCycleStatus);
					jsonObj.put("viewCount", statistics.getViewCount());
					jsonObj.put("likeCount", statistics.getLikeCount());
					jsonObj.put("dislikeCount", statistics.getDislikeCount());
					
					jsonArray.put(jsonObj);
					
					System.out.println("Video as JSON-object added to list");
					
				} catch(JSONException e) {
					// Should never happen
				} catch(Exception e) {
					// The video's privacyStatus has been set to "private" by the user so the video must be ignored
					System.out.println("Video NOT added to list!");
					log.info("A video's privacyStatus has been set to \"private\"");
				}
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject mainObj = new JSONObject();
		try {
			mainObj.put("results", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
//		parameter = "{'videos': [{'id':'5', 'id2':7}]}";
		return mainObj.toString();
	}

	private List<BroadcastEntity> searchInDatabase(String searchTerm) {

		log.info("Search for broadcasts of channel: " + searchTerm);
		System.out.println("Search for broadcasts of channel: " + searchTerm);
		
		List<BroadcastEntity> resultList = new ArrayList<BroadcastEntity>();
		List<BroadcastEntity> broadcastList = ofy().load().type(BroadcastEntity.class).list();

		if (searchTerm == null || searchTerm.equals("")) {
			resultList = broadcastList;
		} else {
			
			Iterator<BroadcastEntity> iterator = broadcastList.iterator();
			
			while (iterator.hasNext()) {
				BroadcastEntity entity = iterator.next();
				if (entity.channelName.equals(searchTerm)) {
					resultList.add(entity); // Add broadcast id to resultList
				}
			}
		}	
		System.out.println("Searched for broadcasts of channel: " + searchTerm);
		
		return resultList;
	}
	
	private List<BroadcastEntity> sortResultList(List<BroadcastEntity> resultList) {
		// Sort list according to the broadcasts' startTime
		Collections.sort(resultList, Collections.reverseOrder());
		return resultList;
	}
	
}

