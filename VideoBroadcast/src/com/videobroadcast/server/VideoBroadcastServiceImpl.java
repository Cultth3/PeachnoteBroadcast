package com.videobroadcast.server;


import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.mortbay.log.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
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
import com.google.api.services.youtube.model.MonitorStreamInfo;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.ObjectifyService;
import com.videobroadcast.client.VideoBroadcastService;
import com.videobroadcast.shared.AuthData;
import com.videobroadcast.shared.BroadcastInfo;
import com.videobroadcast.shared.ErrorCodes;
import com.videobroadcast.shared.IdNameTuple;
import com.videobroadcast.shared.MyConstants;


public class VideoBroadcastServiceImpl extends RemoteServiceServlet implements VideoBroadcastService {

	private final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private final static JacksonFactory JSON_FACTORY = new JacksonFactory();
	
	private static YouTube youtube;
	
	private List<String> wikiPagesList = new ArrayList<String>();
	private boolean wikiPagesInstantiated = false;
	
	MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	private YouTube unauthorizedYoutube;
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
	public VideoBroadcastServiceImpl() {
		df.setTimeZone(TimeZone.getDefault()); // never called?
	}
	
	
	/* Register Objectify entities */
	static {
        ObjectifyService.register(BroadcastEntity.class);
    }

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
	public BroadcastInfo createBroadcast(String token, String titleOfPiece, String privacyStatus, String format) {
		
		 GWT.log("Create Broadcast");

		 df.setTimeZone(TimeZone.getDefault());
		 
		 BroadcastInfo info = new BroadcastInfo();
			
	        try {
	        	
	            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token))
	                    .setApplicationName(AuthData.APPLICATION_NAME).build();
	            
	            // Get channel name to add it to the broadcast title
	            YouTube.Channels.List channelList =  youtube.channels().list("snippet");
	            channelList.setMine(true);
	            ChannelListResponse channelListResponse = channelList.execute();
	            String channelName = channelListResponse.getItems().get(0).getSnippet().getTitle(); 
	            
	            /* A Youtube video title can't have an infinite length */
	            String broadcastTitle;	
	            if (titleOfPiece.concat(" - ").concat(channelName).length() >= 93) {
	            	int pieceTitleLength = 90 - channelName.length();
	            	broadcastTitle = titleOfPiece.substring(0, pieceTitleLength) + "... - " + channelName;
	            } else {
	            	broadcastTitle = titleOfPiece + " - " + channelName;
	            }
	            
	            // Create a snippet with the title and scheduled start and end times for the broadcast. 
	            LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
	            broadcastSnippet.setTitle(broadcastTitle);
	            
	            long futureDateMillis = System.currentTimeMillis() + 1000; 	// ScheduledStartTime must be in future 
	            String currentTimeAsIso = getTimeAsIso(futureDateMillis);
	            broadcastSnippet.setScheduledStartTime(new DateTime(currentTimeAsIso));
	            
	            // Status
	            LiveBroadcastStatus status = new LiveBroadcastStatus();
            	status.setPrivacyStatus(privacyStatus);
	            status.setRecordingStatus("recording");
	            
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
	            LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();	// Backend error can occur

	            // Print information from the API response.
	            System.out.println("\n================== Returned Broadcast ==================\n");
	            System.out.println("  - Id: " + returnedBroadcast.getId());
	            System.out.println("  - Title: " + returnedBroadcast.getSnippet().getTitle());
	            System.out.println("  - Description: " + returnedBroadcast.getSnippet().getDescription());
	            System.out.println("  - Published At: " + returnedBroadcast.getSnippet().getPublishedAt());
	            System.out.println("  - Scheduled Start Time: " + returnedBroadcast.getSnippet().getScheduledStartTime());
	            System.out.println("  - Scheduled End Time: " + returnedBroadcast.getSnippet().getScheduledEndTime());

	            // Define the content distribution network settings for the
	            // video stream. The settings specify the stream's format and
	            // ingestion type. See:
	            // https://developers.google.com/youtube/v3/live/docs/liveStreams#cdn
	            CdnSettings cdnSettings = new CdnSettings();
	            if (format == null) {
	            	format = "360p";
	            }
	            cdnSettings.setFormat(format); 
	            cdnSettings.setIngestionType("rtmp");

	            String streamTitle = format + " - " + currentTimeAsIso;
	            System.out.println("You chose " + streamTitle + " for stream title.");

	            // Create a snippet with the video stream's title.
	            LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
	            streamSnippet.setTitle(streamTitle);
	            
	            LiveStream stream = new LiveStream();	
	            stream.setKind("youtube#liveStream");
	            stream.setSnippet(streamSnippet);
	            stream.setCdn(cdnSettings);

	            // Construct and execute the API request to insert the stream.
	            YouTube.LiveStreams.Insert liveStreamInsert =
	                    youtube.liveStreams().insert("snippet,cdn", stream);
	            LiveStream returnedStream = liveStreamInsert.execute();

	            // Print information from the API response.
	            System.out.println("\n================== Returned Stream ==================\n");
	            System.out.println("  - Id: " + returnedStream.getId());
	            System.out.println("  - Title: " + returnedStream.getSnippet().getTitle());
//	            System.out.println("  - Description: " + returnedStream.getSnippet().getDescription());
//	            System.out.println("  - Published At: " + returnedStream.getSnippet().getPublishedAt());

	            // Construct and execute a request to bind the new broadcast and stream.
	            YouTube.LiveBroadcasts.Bind liveBroadcastBind =
	                    youtube.liveBroadcasts().bind(returnedBroadcast.getId(), "id,contentDetails");
	            liveBroadcastBind.setStreamId(returnedStream.getId());
	            returnedBroadcast = liveBroadcastBind.execute();

	            info.setLiveStreamId(returnedStream.getId());
	            info.setBroadcastId(returnedBroadcast.getId());
	            info.setChannelName(channelName);
	            info.setChannelId(channelListResponse.getItems().get(0).getId());
	            info.setBroadcastTitle(broadcastTitle);
	            
	            // Print information from the API response.
	            System.out.println("\n================== Returned Bound Broadcast ==================\n");
	            System.out.println("  - Broadcast Id: " + returnedBroadcast.getId());
	            System.out.println("  - Bound Stream Id: " + returnedBroadcast.getContentDetails().getBoundStreamId());

	        } catch (GoogleJsonResponseException e) {
	            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
	            
	            // If user is not enabled for live streaming
	            if (e.getDetails().getMessage().equals("The user is not enabled for live streaming.")) {
	            	info.setErrorCode(ErrorCodes.ERROR_LIVESTREAMING_NOT_ACTIVATED);
	            	return info;
	            } else if(e.getDetails().getMessage().equals("Backend Error")) {
	            	System.out.println("Backend error caught!");
	            	info.setErrorCode(ErrorCodes.BACKEND_ERROR);
	            	return info;
	            }
	            	
	            e.printStackTrace();
	        } catch (IOException e) {
	            System.err.println("IOException: " + e.getMessage());
	            e.printStackTrace();
			} catch (Throwable t) {
	            System.err.println("Throwable: " + t.getMessage());
	            t.printStackTrace();
	        }
		 
	        return info;
	        
	}

	@Override
	public void myUploads(String token) {

		try {
			
	    	youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token)).setApplicationName(
	    			AuthData.APPLICATION_NAME).build();
	    	
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
//             if (playlistItem.getSnippet().getTitle().equals("Live stream")) {
            	System.out.println(" video name  = " + playlistItem.getSnippet().getTitle());
            	System.out.println(" video id    = " + playlistItem.getContentDetails().getVideoId());
            	System.out.println(" upload date = " + playlistItem.getSnippet().getPublishedAt());
            	System.out.println("\n-------------------------------------------------------------\n");
//        }
        }
    }

	public void addBroadcastToDatastore(String broadcastId, String channelName, String channelId, String piece, 
											String broadcastTitle, long liveEndTime, String privacyStatus, boolean isLinked) {

		BroadcastEntity broadcastEntity = new BroadcastEntity();
		broadcastEntity.id = broadcastId;
		broadcastEntity.channelName = channelName;
		broadcastEntity.channelId = channelId;
		broadcastEntity.piece = piece;
//		broadcastEntity.broadcastTitle = broadcastTitle;
		
		broadcastEntity.startTime = System.currentTimeMillis();
		broadcastEntity.date = getTimeAsIso(System.currentTimeMillis());
			
		if (!isLinked) {
			broadcastEntity.deletionTime = System.currentTimeMillis() + MyConstants.MAX_TIME_IN_DATASTORE;
			broadcastEntity.liveEndTime = liveEndTime; 
			broadcastEntity.privacyStatus = privacyStatus;

		} else {
			/* Video is not a new broadcast but was already recorded */
			broadcastEntity.lifeCycleStatus = "complete";
			broadcastEntity.stayOnIMSLP = true;
			
		}
			
		ofy().save().entity(broadcastEntity).now();
		
		// Flush memcache
		String key = "myList";
		syncCache.clearAll();
		
	}	
	
	
	public List<IdNameTuple> loadBroadcastListFromDatastore() {

		List<BroadcastEntity> broadcastList = ofy().load().type(BroadcastEntity.class).list();
		List<IdNameTuple> list = new ArrayList<IdNameTuple>();
		
		for (BroadcastEntity x:broadcastList) {
			list.add(new IdNameTuple(x.id, x.channelName));
		}
		
		return list;
	}	
	
	
	@Override
	public long makeBroadcastLive(String token, String broadcastId, String liveStreamId, String channelId,  String channelName, String titleOfPiece, 
												String broadcastTitle, String privacyStatus) {

//		boolean isBroadcastLive = false; 
		long liveEndTime = 0;
		
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token)).setApplicationName(
				AuthData.APPLICATION_NAME).build();
		
		String currentStreamStatus = checkStreamStatus(liveStreamId); // Check if returned stream is "active"
		// If stream is active trigger transition to LifeCycleStatus "live"
		if (currentStreamStatus.equals("active")) {
			final YouTube.LiveBroadcasts.Transition liveBroadcastTransition;
			try {
				liveBroadcastTransition = youtube.liveBroadcasts().transition("live", broadcastId, "status");
				try {
					liveBroadcastTransition.execute();
				} catch (GoogleJsonResponseException e) {
					if (e.getDetails().getMessage().equals("Redundant transition")) {
						System.out.println("GoogleJsonResponseException \"Redundant transition\" caught."
								+ " The client calls makeBroadcastLive() several times because the"
								+ " Youtube server needs time to notice that the stream comes in and to set the transition."
								+ " Because of this slowness it is normal that makeBroadcastLive() can be called redundantly.");
					}
				}
				// Broadcast will only be added to datastore if the transition to "live" didn't fail
				liveEndTime = System.currentTimeMillis() + MyConstants.MAX_TIME_LIVE;
				addBroadcastToDatastore(broadcastId, channelName, channelId, titleOfPiece, broadcastTitle, liveEndTime, privacyStatus, false); 
//				isBroadcastLive = true;	
				System.out.println("Success: Broadcast transition to status \"live\" is triggered and broadcast is added to datastore");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failure: Broadcast transition to status \"live\" is not triggered. EXCEPTION!");
			}
			
		} else {
			System.out.println("Failure: Broadcast transition to status \"live\" is not triggered. No incoming stream from Wirecast.");
		}
		
		return liveEndTime;
	}
	
	public String checkStreamStatus(String liveStreamId) {

		String currentStreamStatus = "";
		YouTube.LiveStreams.List liveStreamRequest;
		try {
			liveStreamRequest = youtube.liveStreams().list("status");
			liveStreamRequest.setId(liveStreamId);
			LiveStreamListResponse response = liveStreamRequest.execute();
			List<LiveStream> returnedList = response.getItems();
			currentStreamStatus = returnedList.get(0).getStatus().getStreamStatus();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return(currentStreamStatus);

	}

	public static String getTimeAsIso(long millis) {
		
//		Date date = new Date();
//        date.setTime(millis);
        String timeAsIso = df.format(millis);
		
		return timeAsIso;
	}
	
	@Override
	public boolean stopStreaming(String token, String broadcastId, long liveEndTime) throws Exception {
		
		boolean isBroadcastLive = true;
		
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token)).setApplicationName(
				AuthData.APPLICATION_NAME).build();	
		
		if (liveEndTime < System.currentTimeMillis()) {
			// MAX_TIME_LIVE exceeded. Cron job 1 will delete the broadcast reference from datastore.
			GWT.log("User tried to stop stream but broadcast was already expired (MAX_TIME_LIVE)");
			System.out.println("User tried to stop stream but broadcast was already expired (MAX_TIME_LIVE)");
//			try {
//				YouTube.LiveBroadcasts.Transition liveBroadcastTransition = youtube.liveBroadcasts().transition("complete", returnedBroadcast.getId(), "id");
//				liveBroadcastTransition.execute();
//			} catch (Exception e) {
//				// Nice try but do nothing anymore
//			}
			throw new Exception("MAX_TIME_LIVE exceeded");
		} else {
		
			try {
				
				try {
					// Stop stream by setting broadcast event to the status "complete"
					YouTube.LiveBroadcasts.Transition liveBroadcastTransition = youtube.liveBroadcasts().transition("complete", broadcastId, "id");
					liveBroadcastTransition.execute();
					isBroadcastLive = false;
					try {
						BroadcastEntity broadcastEntity = ofy().load().type(BroadcastEntity.class).id(broadcastId).now();
						broadcastEntity.lifeCycleStatus = "complete";
						broadcastEntity.endTime = System.currentTimeMillis();
						ofy().save().entity(broadcastEntity).now();
						syncCache.clearAll();
					} catch (Exception e) {
						Log.warn("Broadcast has been deleted from datastore before user stopped streaming");
						throw new Exception("Video is already deleted from datastore");
					}
				} catch (GoogleJsonResponseException e) {
					if (e.getDetails().getMessage().equals("Backend error")) {
						isBroadcastLive = false;	// Transition sollte trotzdem getriggert sein?
						GWT.log("CAUGHT EXCEPTION: (Youtube) Backend error");
						System.out.println("Caught exception: (Youtube) Backend error");
					}
					if (e.getDetails().getMessage().equals("Redundant transition")) {
						// Broadcast was already stopped manually by user in his account
						GWT.log("Caught exception: Redundant transition");
						System.out.println("CAUGHT EXCEPTION: Redundant transition");
						try {
							deleteBroadcastFromDatastore(broadcastId);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						throw new Exception("Redundant transition");
					}
				}
				
			} catch (Exception e) {
				if (e.getMessage().equals("Redundant transition")) {
					throw new Exception("Redundant transition");
				} else if (e.getMessage().equals("Video is already deleted from datastore")) {
					throw new Exception("Video is already deleted from datastore");
				}
				e.printStackTrace();
				
			}
		}
		
		return isBroadcastLive;
		
	}
		

	@Override
	public String getChannelId(String token) {
		 youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token))
         .setApplicationName(AuthData.APPLICATION_NAME).build();
 
		 YouTube.Channels.List channelList;
		try {
			channelList = youtube.channels().list("id");
			channelList.setMine(true);
			ChannelListResponse channelListResponse = channelList.execute();
			return channelListResponse.getItems().get(0).getId(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void deleteBroadcastAndStream(String token, String broadcastId, String liveStreamId) {
		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, createCredential(token)).setApplicationName(
				AuthData.APPLICATION_NAME).build();	
		
		YouTube.LiveBroadcasts.Delete liveBroadcastDelete;
		
		try {
			
			liveBroadcastDelete = youtube.liveBroadcasts().delete(broadcastId);
			liveBroadcastDelete.execute();
			
			GWT.log("Broadcast with ID " + broadcastId + " is deleted from the user's Youtube"
					+ " account!");
			System.out.println("Broadcast with ID " + broadcastId + " is deleted from the user's Youtube"
					+ " account!");
			
			// Delete live stream
			YouTube.LiveStreams.Delete liveStreamDelete = youtube.liveStreams().delete(liveStreamId);
			liveStreamDelete.execute();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteBroadcastCompletely(String token, String broadcastId, String liveStreamId) {
		try {
			deleteBroadcastFromDatastore(broadcastId); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			deleteBroadcastAndStream(token, broadcastId, liveStreamId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void deleteBroadcastFromDatastore(String broadcastId) {
		try {
			BroadcastEntity broadcastEntity = ofy().load().type(BroadcastEntity.class).id(broadcastId).now();
			ofy().delete().entity(broadcastEntity).now();
			syncCache.clearAll();
			GWT.log("Broadcast with ID " + broadcastId + " is removed from datastore!");
			System.out.println("Broadcast with ID " + broadcastId + " is removed from datastore!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		ofy().delete().entity(new Key<BroadcastEntity>(BroadcastEntity.class, returnedBroadcast.getId()));

	}

	@Override
	public boolean instantiateWikiPages(String title) {
		
		if (!wikiPagesInstantiated) {
		    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));	// Just to avoid init method
			
			ServletContext context = getServletContext();   
			InputStream input = context.getResourceAsStream("/WEB-INF/resources/wiki-pages");
			
			try {
				Reader in = new InputStreamReader(input,  "UTF-8");
				Scanner scanner;
				scanner = new Scanner(in);
				while(scanner.hasNextLine()){
					wikiPagesList.add(scanner.nextLine()); 
				}
				scanner.close();
				wikiPagesInstantiated = true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return (title != null) && checkPiece(title);
			
	}

	@Override
	public List<String> getWikiPages(String text) {
		
		
			int limitCount = 0;
			
			List<String> resultList = new ArrayList<>();
			Iterator<String> iterator = wikiPagesList.iterator();
			
		try {
			while (iterator.hasNext() && limitCount < MyConstants.WIKI_PAGES_RESULT_LIMIT) {
				String string = iterator.next();
				if (Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher(string).find()) { // string.contains(text)
					resultList.add(string);
					limitCount++;
	//				System.out.println(string);
				}
			}
		} catch (Exception e) {
			Log.warn(e.toString());
			Log.warn(e.getStackTrace().toString());
		}
		return resultList;
	}


	@Override
	public boolean checkPiece(String title) {
		return wikiPagesList.contains(title);
	}


	@Override
	public boolean doFinalPieceCheck(String selectedPiece) {
		return wikiPagesList.contains(selectedPiece);
	}

	@Override
	public void setBroadcastStaysOnIMSLP(boolean stayOnIMSLP, String id) {
		try {
			BroadcastEntity broadcastEntity = ofy().load().type(BroadcastEntity.class).id(id).now();
			if (stayOnIMSLP) {
				broadcastEntity.stayOnIMSLP = true;
				ofy().save().entity(broadcastEntity).now();
			} else {
				if (broadcastEntity.stayOnIMSLP) {
					broadcastEntity.stayOnIMSLP = false;
					ofy().save().entity(broadcastEntity).now();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean checkIfVideoIsAvailable(String videoId, String pieceToLink) {

		System.out.println(videoId);
		
		unauthorizedYoutube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName(AuthData.APPLICATION_NAME).build();
		
		YouTube.Videos.List videoList;
		try {
			videoList = unauthorizedYoutube.videos().list("id, snippet");
			videoList.setId(videoId); 
			videoList.setKey(AuthData.API_KEY);
			VideoListResponse response = videoList.execute();
			System.out.println(response);
			try {
				response.getItems().get(0).getId(); // Throws an exception if the video doesn't exist or ist not accessible
//				System.out.println("Video exists!");
				
				String title = response.getItems().get(0).getSnippet().getTitle();
				String channelId = response.getItems().get(0).getSnippet().getChannelId();
				String channelName = response.getItems().get(0).getSnippet().getChannelTitle(); 
				
				addBroadcastToDatastore(videoId, channelName, channelId, pieceToLink, title, 0, "", true);
				return true;
			} catch (Exception e2) {
//				System.out.println("Video does NOT exist!");
				return false;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}

	
}