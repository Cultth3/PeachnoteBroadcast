package com.videobroadcast.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.ObjectifyService;
import com.videobroadcast.client.TVListService;
import com.videobroadcast.shared.AuthData;
import com.videobroadcast.shared.BroadcastInfo;

/**
 * 
 * This is the second RemoteServiceServlet that manages requests from the user's
 * TV section. It returns the video lists for a given search term and deletes
 * videos of the list if the user is authorized to do so.
 * 
 * @author Tom
 *
 */

@SuppressWarnings("serial")
public class TVListServiceImpl extends RemoteServiceServlet implements TVListService {
	
	private final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private final static JacksonFactory JSON_FACTORY = new JacksonFactory();
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	private static final Logger log = Logger
			.getLogger(TVListServiceImpl.class.getName());

	static {
        ObjectifyService.register(BroadcastEntity.class);
    }
	
	private static YouTube youtube;
	
	private GoogleCredential createCredential(String token) {
	   	
    	GoogleCredential credential = new GoogleCredential.Builder()
    			.setTransport(new NetHttpTransport())
    			.setJsonFactory(new JacksonFactory())
    			.setClientSecrets(AuthData.CLIENT_ID, AuthData.CLIENT_SECRET)
    			.build();
    	
    	credential.setAccessToken(token);
    	return credential;
    	
	}
	
	@Override
	public List<BroadcastInfo> giveVideoList(String searchTerm) {

		List<BroadcastEntity> searchList = searchInDatabase(searchTerm); 
		List<BroadcastEntity> sortedList = sortResultList(searchList);
		
		List<BroadcastInfo> resultList = new ArrayList<BroadcastInfo>();
		
		for (BroadcastEntity entity:sortedList) {
			BroadcastInfo info = new BroadcastInfo();
			info.setBroadcastId(entity.id);
			info.setBroadcastTitle(entity.piece); // Actually we don't use the broadcastTitle but the piece title on purpose
			info.setChannelName(entity.channelName);
			info.setLifeCycleStatus(entity.lifeCycleStatus);
			info.setChannelId(entity.channelId);
			resultList.add(info);
		}
		
		return resultList;
	}

	private List<BroadcastEntity> searchInDatabase(String searchTerm) {

		List<BroadcastEntity> resultList = new ArrayList<BroadcastEntity>();
		List<BroadcastEntity> broadcastList = ofy().load().type(BroadcastEntity.class).list();

		if (searchTerm == null || searchTerm.equals("")) {
			resultList = broadcastList;
		} else {
			
			Iterator<BroadcastEntity> iterator = broadcastList.iterator();
			
			/* First search for channels */
			while (iterator.hasNext()) {
				BroadcastEntity entity = iterator.next();
//				if (entity.channelName.equals(searchTerm)) {
				if (Pattern.compile(Pattern.quote(searchTerm), Pattern.CASE_INSENSITIVE).matcher(entity.channelName).find()
						|| Pattern.compile(Pattern.quote(searchTerm), Pattern.CASE_INSENSITIVE).matcher(entity.piece).find()) {
					resultList.add(entity); // Add broadcast id to resultList
				}
			}
			
//			/* If no channel was found, search for pieces */
//			if (resultList.isEmpty()) {
//				Iterator<BroadcastEntity> iterator2 = broadcastList.iterator();
//				while (iterator2.hasNext()) {
//					BroadcastEntity entity = iterator2.next();
//					if (Pattern.compile(Pattern.quote(searchTerm), Pattern.CASE_INSENSITIVE).matcher(entity.piece).find()) {
//						resultList.add(entity); // Add broadcast id to resultList
//					}
//				}
//			}
//			
//			Iterator<BroadcastEntity> iterator2 = broadcastList.iterator();
//			while (iterator2.hasNext()) {
//				BroadcastEntity entity = iterator2.next();
//				if (Pattern.compile(Pattern.quote(searchTerm), Pattern.CASE_INSENSITIVE).matcher(entity.piece).find()) {
//					resultList.add(entity); 
//				}
//			}
				
			
		}
		return resultList;
	}

	private List<BroadcastEntity> sortResultList(List<BroadcastEntity> resultList) {
		Collections.sort(resultList, Collections.reverseOrder());
		return resultList;
	}

	@Override
	public String deleteVideo(String token, String broadcastId) {
		
		String message = "deletion failed";
		
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token))
        .setApplicationName(AuthData.APPLICATION_NAME).build();

		
		try {
			YouTube.Channels.List channelList =  youtube.channels().list("id");
            channelList.setMine(true);
            ChannelListResponse channelListResponse = channelList.execute();
            String channelId = channelListResponse.getItems().get(0).getId();
            
            BroadcastEntity broadcastEntity = ofy().load().type(BroadcastEntity.class).id(broadcastId).now();
            
            try {
	            if (broadcastEntity.channelId.equals(channelId)) {
	            	ofy().delete().entity(broadcastEntity).now();
	    			message = "deletion succeeded";
	    			log.warning("Broadcast " + broadcastId
	    					+ " has been deleted from datastore (User deleted it in the TV section)!");
	    		} else {
	    			message = "no permission";
	    		}
            } catch (NullPointerException e) {
            	log.warning("Video already has been deleted but because of the slowness of the datastore the TV section list has not been refreshed correctly");
            }
            
            
		} catch (IOException e) {		
			e.printStackTrace();
		} 
		
		return message;
	}


	
}