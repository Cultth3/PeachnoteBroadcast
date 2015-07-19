package com.videobroadcast.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.videobroadcast.shared.BroadcastInfo;

public interface TVListServiceAsync {

	void giveVideoList(String searchTerm,
			AsyncCallback<List<BroadcastInfo>> asyncCallback);

	void deleteVideo(String token, String broadcastId,
			AsyncCallback<String> asyncCallback);

	

}
