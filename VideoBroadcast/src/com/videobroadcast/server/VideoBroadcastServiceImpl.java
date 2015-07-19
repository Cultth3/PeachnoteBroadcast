package com.videobroadcast.server;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastContentDetails;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamListResponse;
import com.google.api.services.youtube.model.LiveStreamSnippet;
import com.google.api.services.youtube.model.LiveStreamStatus;
import com.google.api.services.youtube.model.MonitorStreamInfo;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dev.shell.remoteui.MessageTransport.ErrorCallback;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Work;
import com.videobroadcast.client.VideoBroadcastService;
import com.videobroadcast.shared.AuthData;
import com.videobroadcast.shared.ErrorCodes;
import com.videobroadcast.shared.IdNameTuple;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import static com.googlecode.objectify.ObjectifyService.ofy;


public class VideoBroadcastServiceImpl extends RemoteServiceServlet implements VideoBroadcastService {

	private final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private final static JacksonFactory JSON_FACTORY = new JacksonFactory();
	private static final String APPLICATION_NAME = "peachnotebroadcast";
	
	private static YouTube youtube;
	
	private LiveBroadcast returnedBroadcast;  // The actual (last) broadcast which was created
	private LiveStream returnedStream;		  // The actual (last) stream which was created with its corresponding broadcast
	private String broadcastEntity = null;	  // Google App Datastore entity of the actual (last) broadcast which was created
	private String myChannelName;
	private String currentTimeAsIso;
	
	// Register Objectify entities
	static {
        ObjectifyService.register(BroadcastEntity.class);
    }

	
	@Override
	public void g() {
		// test
	}

	@Override
	public String createBroadcast(String token) {
		
		 GWT.log("Create Broadcast");
			
	        try {
	        	
	            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token))
	                    .setApplicationName(APPLICATION_NAME).build();
	            
	            // Get channel name to add it to the broadcast title
	            YouTube.Channels.List channelList =  youtube.channels().list("snippet");
	            channelList.setMine(true);
	            ChannelListResponse channelListResponse = channelList.execute();
	            myChannelName = channelListResponse.getItems().get(0).getSnippet().getTitle(); 
	            	
	            String title = myChannelName + " Broadcast " + new Date(System.currentTimeMillis());
	            
	            // Create a snippet with the title and scheduled start and end times for the broadcast. 
	            LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
	            broadcastSnippet.setTitle(title);
	            
	            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	            df.setTimeZone(TimeZone.getDefault());
	            long futureDateMillis = System.currentTimeMillis() + 1000; 	// ScheduledStartTime must be in future 
	            Date futureDate = new Date();
	            futureDate.setTime(futureDateMillis);
	            currentTimeAsIso = df.format(futureDate);
	            broadcastSnippet.setScheduledStartTime(new DateTime(currentTimeAsIso));
	            
//	            String endTimeAsIso = df.format(new Date(System.currentTimeMillis() + 300000l)); // 300 = 5 min, 1576800000000l = in ~50 Jahren
//	            broadcastSnippet.setScheduledEndTime(new DateTime("2014-11-19T22:56:00.000Z"));
//	            System.out.println(futureDate);
//	            System.out.println(broadcastSnippet.getScheduledStartTime());
	            
	            // Status
	            LiveBroadcastStatus status = new LiveBroadcastStatus();
	            status.setPrivacyStatus("unlisted");
	            status.setRecordingStatus("recording");
//	            status.setLifeCycleStatus("live");
	            
	            // ContentDetails
	            LiveBroadcastContentDetails contentDetails = new LiveBroadcastContentDetails();
	            contentDetails.setMonitorStream(new MonitorStreamInfo().setEnableMonitorStream(false));
	            contentDetails.setRecordFromStart(true);
	            contentDetails.setEnableDvr(true);
	            contentDetails.setEnableEmbed(true);

	            LiveBroadcast broadcast = new LiveBroadcast();
	            broadcast.setKind("youtube#liveBroadcast");
	            broadcast.setSnippet(broadcastSnippet);
	            broadcast.setContentDetails(contentDetails);
	            broadcast.setStatus(status);


	            // Construct and execute the API request to insert the broadcast.
	            YouTube.LiveBroadcasts.Insert liveBroadcastInsert =
	                    youtube.liveBroadcasts().insert("snippet,status,contentDetails", broadcast);
	            returnedBroadcast = liveBroadcastInsert.execute();

	            // Print information from the API response.
	            System.out.println("\n================== Returned Broadcast ==================\n");
	            System.out.println("  - Id: " + returnedBroadcast.getId());
	            System.out.println("  - Title: " + returnedBroadcast.getSnippet().getTitle());
	            System.out.println("  - Description: " + returnedBroadcast.getSnippet().getDescription());
	            System.out.println("  - Published At: " + returnedBroadcast.getSnippet().getPublishedAt());
	            System.out.println("  - Scheduled Start Time: " + returnedBroadcast.getSnippet().getScheduledStartTime());
	            System.out.println("  - Scheduled End Time: " + returnedBroadcast.getSnippet().getScheduledEndTime());

	            // (Prompt the user to enter a title for the video stream.)
	            title = "Stream 360p " + currentTimeAsIso;
	            System.out.println("You chose " + title + " for stream title.");

	            // Create a snippet with the video stream's title.
	            LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
	            streamSnippet.setTitle(title);

	            // Define the content distribution network settings for the
	            // video stream. The settings specify the stream's format and
	            // ingestion type. See:
	            // https://developers.google.com/youtube/v3/live/docs/liveStreams#cdn
	            CdnSettings cdnSettings = new CdnSettings();
	            cdnSettings.setFormat("360p");
	            cdnSettings.setIngestionType("rtmp");

	            LiveStream stream = new LiveStream();	
	            stream.setKind("youtube#liveStream");
	            stream.setSnippet(streamSnippet);
	            stream.setCdn(cdnSettings);

	            // Construct and execute the API request to insert the stream.
	            YouTube.LiveStreams.Insert liveStreamInsert =
	                    youtube.liveStreams().insert("snippet,cdn", stream);
	            returnedStream = liveStreamInsert.execute();

	            // Print information from the API response.
	            System.out.println("\n================== Returned Stream ==================\n");
	            System.out.println("  - Id: " + returnedStream.getId());
	            System.out.println("  - Title: " + returnedStream.getSnippet().getTitle());
	            System.out.println("  - Description: " + returnedStream.getSnippet().getDescription());
	            System.out.println("  - Published At: " + returnedStream.getSnippet().getPublishedAt());

	            // Construct and execute a request to bind the new broadcast and stream.
	            YouTube.LiveBroadcasts.Bind liveBroadcastBind =
	                    youtube.liveBroadcasts().bind(returnedBroadcast.getId(), "id,contentDetails");
	            liveBroadcastBind.setStreamId(returnedStream.getId());
//	            liveBroadcastBind.setStreamId("NrqNln-FAgA68-LXVgh4Zg1414504603836227");
	            returnedBroadcast = liveBroadcastBind.execute();
	            
	            // Print information from the API response.
	            System.out.println("\n================== Returned Bound Broadcast ==================\n");
	            System.out.println("  - Broadcast Id: " + returnedBroadcast.getId());
	            System.out.println("  - Bound Stream Id: " + returnedBroadcast.getContentDetails().getBoundStreamId());

	        } catch (GoogleJsonResponseException e) {
	            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
	            
	            // If user is not enabled for live streaming
	            if (e.getDetails().getMessage().equals("The user is not enabled for live streaming.")) {
	            	return ErrorCodes.ERROR_LIVESTREAMING_NOT_ACTIVATED;
	            }
	            e.printStackTrace();
	        } catch (IOException e) {
	            System.err.println("IOException: " + e.getMessage());
	            e.printStackTrace();
	        } catch (Throwable t) {
	            System.err.println("Throwable: " + t.getMessage());
	            t.printStackTrace();
	        }
	        
	        if (returnedBroadcast.getId() != null)
	        	return returnedBroadcast.getId();
	        else 
	        	return null;
		
	}

	@Override
	public void myUploads(String token) {

		try {
    	
	    	youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token)).setApplicationName(
	    			APPLICATION_NAME).build();
	    	
	    	YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
	        channelRequest.setMine(true);
	        channelRequest.setFields("items/contentDetails,nextPageToken,pageInfo");
	        ChannelListResponse channelResult = channelRequest.execute();
	
	        List<Channel> channelsList = channelResult.getItems();
	
	        if (channelsList != null) {
	            // The user's default channel is the first item in the list.
	            // Extract the playlist ID for the channel's videos from the
	            // API response.
	            String uploadPlaylistId =
	                    channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();
	
	            // Define a list to store items in the list of uploaded videos.
	            List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();
	
	            // Retrieve the playlist of the channel's uploaded videos.
	            YouTube.PlaylistItems.List playlistItemRequest = null;
				playlistItemRequest = youtube.playlistItems().list("id,contentDetails,snippet");
	            playlistItemRequest.setPlaylistId(uploadPlaylistId);

	            // Only retrieve data used in this application, thereby making
	            // the application more efficient. See:
	            // https://developers.google.com/youtube/v3/getting-started#partial
	            playlistItemRequest.setFields(
	                    "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");
	
	            String nextToken = "";
	
	            // Call the API one or more times to retrieve all items in the
	            // list. As long as the API response returns a nextPageToken,
	            // there are still more items to retrieve.
	            do {
	                playlistItemRequest.setPageToken(nextToken);
	                PlaylistItemListResponse playlistItemResult = null;
					playlistItemResult = playlistItemRequest.execute();
	
	                playlistItemList.addAll(playlistItemResult.getItems());
	
	                nextToken = playlistItemResult.getNextPageToken();
	            } while (nextToken != null);
	
	            // Prints information about the results.
	            prettyPrint(playlistItemList.size(), playlistItemList.iterator());
	        }
	        
	    } catch (Throwable t) {
	    	t.printStackTrace();
	    }

	}
	
	private static void prettyPrint(int size, Iterator<PlaylistItem> playlistEntries) {
        System.out.println("=============================================================");
        System.out.println("\t\tTotal Videos Uploaded: " + size);
        System.out.println("=============================================================\n");

        while (playlistEntries.hasNext()) {
            PlaylistItem playlistItem = playlistEntries.next();
//            if (playlistItem.getSnippet().getTitle().equals("Live stream")) {
            	System.out.println(" video name  = " + playlistItem.getSnippet().getTitle());
            	System.out.println(" video id    = " + playlistItem.getContentDetails().getVideoId());
            	System.out.println(" upload date = " + playlistItem.getSnippet().getPublishedAt());
            	System.out.println("\n-------------------------------------------------------------\n");
//        }
        }
    }

	private void setBroadcastStatus(String broadcastId, String status) {
		
		try {
			YouTube.LiveBroadcasts.List liveBroadcastRequest =
			        youtube.liveBroadcasts().list("id,snippet");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private GoogleCredential createCredential(String token) {
	   	
    	GoogleCredential credential = new GoogleCredential.Builder()
    			.setTransport(new NetHttpTransport())
    			.setJsonFactory(new JacksonFactory())
    			.setClientSecrets(AuthData.CLIENT_ID, AuthData.CLIENT_SECRET)
    			.build();
    	
//    	credential.setRefreshToken(refreshToken);
    	credential.setAccessToken(token);
    	return credential;
    	
	}

	@Override
	public String getBroadcastId(String token, String id) {
		
//		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token)).setApplicationName(
//                APPLICATION_NAME).build();
		
//		returnedStream.setStatus(new LiveStreamStatus().setStreamStatus("active"));
//        returnedBroadcast.setStatus(new LiveBroadcastStatus().setLifeCycleStatus("live"));
		
//		String bid = returnedBroadcast.getId();
//		LiveBroadcastStatus status = null;
//		LiveStreamStatus streamStatus = null;
//		try {
////			status = returnedBroadcast.getStatus();
//			streamStatus = returnedStream.getStatus();
//		} catch (Exception e) {}
//		if (streamStatus == null) {
//			System.out.println("Status is null");
//			return "id: " + bid;
//		}
//		else {
//			return "id: " + bid + "\n status: " + streamStatus; 
//		}
		
		
		addBroadcastToDatastore("D62_yDe1HYQ", "Channel 1");
		addBroadcastToDatastore("ylLzyHk54Z0", "Channel 2");
		addBroadcastToDatastore("tVIIgcIqoPw", "Channel 3");
		addBroadcastToDatastore("I8xZBfVsMzs", "Channel 4");
		
		return "something";
		
	}

	public void addBroadcastToDatastore(String id, String channelName) {

		BroadcastEntity broadcastEntity = new BroadcastEntity();
		broadcastEntity.id = id;
		broadcastEntity.channelName = channelName;
		broadcastEntity.date = currentTimeAsIso;
		
		ofy().save().entity(broadcastEntity).now();
		
	}	
	
	public void deleteBroadcastDromDatastore(String id) {
		BroadcastEntity broadcastEntity = ofy().load().type(BroadcastEntity.class).id(id).now();
		ofy().delete().entity(broadcastEntity);
		
//		ofy().delete().entity(new Key<BroadcastEntity>(BroadcastEntity.class, returnedBroadcast.getId()));

	}
	
	public List<IdNameTuple> loadBroadcastListFromDatastore() {

		List<BroadcastEntity> broadcastList = ofy().load().type(BroadcastEntity.class).filter("index", "0").list();
//		BroadcastID bid = ofy().load().type(BroadcastID.class).id("D62_yDe1H1YQ").now(); 

//		List<String> stringList = new ArrayList<String>();
//		for (BroadcastEntity x:broadcastList) {
//			stringList.add(x.id);
//		}
		
		List<IdNameTuple> list = new ArrayList<IdNameTuple>();
		
		for (BroadcastEntity x:broadcastList) {
			list.add(new IdNameTuple(x.id, x.channelName));
		}
		
		return list;
	}	
	
	
	@Override
	public Boolean makeBroadcastLive(String token) {

		boolean isBroadcastLive = false;
		
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token)).setApplicationName(
				APPLICATION_NAME).build();
		
		try {
			// Check if returned stream is "active"
			YouTube.LiveStreams.List liveStreamRequest = youtube.liveStreams().list("status");
			liveStreamRequest.setId(returnedStream.getId());
			LiveStreamListResponse response = liveStreamRequest.execute();
			List<LiveStream> returnedList = response.getItems();
			String currentStreamStatus = returnedList.get(0).getStatus().getStreamStatus();
			
			// As soon as stream is active trigger transition to LifeCycleStatus "live"
			if (currentStreamStatus.equals("active")) {
				YouTube.LiveBroadcasts.Transition liveBroadcastTransition = youtube.liveBroadcasts().transition("live", returnedBroadcast.getId(), "status");
				liveBroadcastTransition.execute();
				isBroadcastLive = true;	
	            addBroadcastToDatastore(returnedBroadcast.getId(), myChannelName); 	// Add to datastore as soon as broadcast is live
				System.out.println("Success: Broadcast transition to status \"live\" is triggered and broadcast is added to datastore");
			} else {
				System.out.println("Failure: Broadcast transition to status \"live\" is not triggered");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isBroadcastLive;
	}

	@Override
	public boolean stopStreaming(String token) {
		
		boolean isBroadcastLive = true;
		
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token)).setApplicationName(
				APPLICATION_NAME).build();	
		
		try {
			
			// Stop stream by setting broadcast event to the status "complete"
			YouTube.LiveBroadcasts.Transition liveBroadcastTransition = youtube.liveBroadcasts().transition("complete", returnedBroadcast.getId(), "id");
			liveBroadcastTransition.execute();
			isBroadcastLive = false;
			
			// Delete broadcast event
			YouTube.LiveBroadcasts.Delete liveBroadcastDelete = youtube.liveBroadcasts().delete(returnedBroadcast.getId());
			liveBroadcastDelete.execute();
			
			// Delete live stream
			YouTube.LiveStreams.Delete liveStreamDelete = youtube.liveStreams().delete(returnedStream.getId());
			liveStreamDelete.execute();
			
			// Remove BroadcastEntity from Datastore
			// TODO: Eventuell Thread/Deamon-Thread, der Broadcast erst eine Stunde später beendet?
			deleteBroadcastDromDatastore(returnedBroadcast.getId());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return isBroadcastLive;
		
	}
	
}
