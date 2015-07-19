package com.videobroadcast.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.videobroadcast.shared.BroadcastInfo;
import com.videobroadcast.shared.IdNameTuple;

public interface VideoBroadcastServiceAsync {

	void createBroadcast(String token, String titleOfPiece,
			String privacyStatus, String format,
			AsyncCallback<BroadcastInfo> callback);

	void myUploads(String token, AsyncCallback<Void> callback);

	void makeBroadcastLive(String token, String broadcastId, String channelId,
			String liveStreamId, String channelName, String titleOfPiece,
			String broadcastTitle, String privacyStatus,
			AsyncCallback<Long> callback);

	void stopStreaming(String token, String broadcastId, long liveEndTime,
			AsyncCallback<Boolean> callback);
	
	void loadBroadcastListFromDatastore(AsyncCallback<List<IdNameTuple>> callback);

	void getChannelId(String token, AsyncCallback<String> callback);

	void deleteBroadcastCompletely(String token, String broadcastId,
			String liveStreamId, AsyncCallback<Void> asyncCallback);
	
	void deleteBroadcastFromDatastore(String id, AsyncCallback<Void> asyncCallback);

	void getWikiPages(String text, AsyncCallback<List<String>> asyncCallback);

	void instantiateWikiPages(String title, AsyncCallback<Boolean> asyncCallback);

	void checkPiece(String title, AsyncCallback<Boolean> asyncCallback);

	void doFinalPieceCheck(String selectedPiece,
			AsyncCallback<Boolean> asyncCallback);

	void setBroadcastStaysOnIMSLP(boolean stayOnIMSLP, String id,
			AsyncCallback<Void> asyncCallback);

	void deleteBroadcastAndStream(String token, String broadcastId,
			String liveStreamId, AsyncCallback<Void> asyncCallback);

	void checkStreamStatus(String liveStreamId,
			AsyncCallback<String> asyncCallback);

	void checkIfVideoIsAvailable(String videoId,
			String pieceToLink, AsyncCallback<Boolean> asyncCallback);

}
