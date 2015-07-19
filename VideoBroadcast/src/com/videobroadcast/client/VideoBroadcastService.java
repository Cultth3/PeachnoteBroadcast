package com.videobroadcast.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.videobroadcast.shared.BroadcastInfo;
import com.videobroadcast.shared.IdNameTuple;

@RemoteServiceRelativePath("videobroadcastservice")
public interface VideoBroadcastService extends RemoteService {

	void myUploads(String token);

	BroadcastInfo createBroadcast(String token, String titleOfPiece,
			String privacyStatus, String format);
	
	List<IdNameTuple> loadBroadcastListFromDatastore();

	String getChannelId(String token);

	void deleteBroadcastFromDatastore(String id);
	
	List<String> getWikiPages(String text);

	boolean instantiateWikiPages(String title);

	boolean checkPiece(String title);

	boolean doFinalPieceCheck(String selectedPiece);

	void setBroadcastStaysOnIMSLP(boolean stayOnIMSLP, String id);

	boolean stopStreaming(String token, String broadcastId, long liveEndTime)
			throws Exception;

	void deleteBroadcastCompletely(String token, String broadcastId,
			String liveStreamId);
	
	void deleteBroadcastAndStream(String token, String broadcastId, String liveStreamId);

	String checkStreamStatus(String liveStreamId);

	long makeBroadcastLive(String token, String broadcastId, String channelId,
			String liveStreamId, String channelName, String titleOfPiece,
			String broadcastTitle, String privacyStatus);

	boolean checkIfVideoIsAvailable(String videoId, String pieceToLink);

}
