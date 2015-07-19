package com.videobroadcast.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
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
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;
import com.videobroadcast.shared.AuthData;

/**
 * 
 * Returns the list of broadcasts + all relevant video information
 * for a requested IMSLP piece. Videos with the privacyStatus "private"
 * won't be sent to the client because these videos can't be watched
 * by everybody.
 * 
 * 
 * @author Tom
 *
 */

@SuppressWarnings("serial")
public class IMSLPListServlet extends HttpServlet {
	
	private final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private final static JacksonFactory JSON_FACTORY = new JacksonFactory();
	private static final String APPLICATION_NAME = "Music Live Broadcasting";
	
	private static YouTube youtube;
	
	private static final Logger log = Logger
			.getLogger(IMSLPListServlet.class.getName());
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	static {
        ObjectifyService.register(BroadcastEntity.class);
    }

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("Get broadcast list");

		log.info("IMSLPListServlet is running");

		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName(APPLICATION_NAME).build();
		
		try {
			
			String jsonData = "{\"results\":[]}";
			
			String title = req.getParameter("title");
			
			String key = "myList";
			List<String> list;

		    // Using the synchronous cache
		    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		    list = (List<String>) syncCache.get(key);
//		    System.out.println("In Memcache: " + list);
		    
		    if (list == null) {
//		    	System.out.println("Memcache was null. Refill Memcache.");
		    	list = new ArrayList<String>();
		    	
		    	boolean alreadyFetchedFromDatastore = false;
		    	List<BroadcastEntity> broadcastList = ofy().load().type(BroadcastEntity.class).list();
		    	for (BroadcastEntity entity : broadcastList) {
		    		String piece = entity.piece;
		    		list.add(piece);		// TODO: Stücke sind doppelt vorhanden!
		    		if (!alreadyFetchedFromDatastore && title.equals(piece))  {
		    			jsonData = getDataAsJson(piece);
		    			alreadyFetchedFromDatastore = true;
		    		}
				}
		    	syncCache.put("myList", list); // populate cache
//		    	System.out.println("Refilled Memcache. Now in Memcache: " + list);
		    } else {
//		    	System.out.println("List is in Memcache!");
//		    	System.out.println("Search in list: " + list);
		    	for (String piece : list) {
		    		if (title.equals(piece)) {	// ???
		    			jsonData = getDataAsJson(piece);
		    			break;
		    		}
				}
		    }
				    

			String output = req.getParameter("callback") + "(" + jsonData + ");";
	
			resp.setContentType("text/javascript");
		          
			PrintWriter out = resp.getWriter();
			out.println(output);
			System.out.println(output);
			
			log.info("IMSLPListServlet has been executed");

		} catch (Exception ex) {
			ex.printStackTrace();
			log.warning("Get Broadcast list of IMSLPListServlet failed!");
			log.warning("" + ex.getStackTrace());
//			log.warning("Message: " + ex.getMessage());
			log.warning("Cause: " + ex.getCause());
		}
	}
	
	
	private String getDataAsJson(String title) {
		
		List<BroadcastEntity> resultList = searchInDatabase(title); 
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
					jsonObj.put("startTime", entity.startTime);
					jsonObj.put("endTime", entity.endTime);
					jsonObj.put("lifeCycleStatus", entity.lifeCycleStatus);
					jsonObj.put("channelName", entity.channelName);
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

	private List<BroadcastEntity> searchInDatabase(String title) {

		log.info("Search for broadcasts of the piece: " + title);
		System.out.println("Search for broadcasts of the piece: " + title);
		
		List<BroadcastEntity> resultList = new ArrayList<BroadcastEntity>();
		
		Query<BroadcastEntity> query = ofy().load().type(BroadcastEntity.class).filter("piece", title);
		for (BroadcastEntity entity: query) {
			resultList.add(entity); 
		}
		
		System.out.println("Searched for broadcasts of the piece: " + title);
		
		return resultList;
	}
	
	private List<BroadcastEntity> sortResultList(List<BroadcastEntity> resultList) {
		// Sort list according to the broadcasts' startTimes
		Collections.sort(resultList, Collections.reverseOrder());
		return resultList;
	}
	
}

