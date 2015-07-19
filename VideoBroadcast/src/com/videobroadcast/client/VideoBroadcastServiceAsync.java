package com.videobroadcast.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface VideoBroadcastServiceAsync {

	void g(AsyncCallback callback);

	void createBroadcast(String token, AsyncCallback<String> callback);

	void myUploads(String token, AsyncCallback callback);

	void getBroadcastId(String id, String string, AsyncCallback<String> callback);

	void makeBroadcastLive(String token, AsyncCallback callback);

	void stopStreaming(String token, AsyncCallback callback);
	
	void loadBroadcastListFromDatastore(AsyncCallback callback);

}
