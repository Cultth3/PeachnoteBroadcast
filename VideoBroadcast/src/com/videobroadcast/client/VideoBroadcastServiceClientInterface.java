package com.videobroadcast.client;

import java.util.List;

import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface VideoBroadcastServiceClientInterface {

	void g();

	void createBroadcast();

	void myUploads();
	
	void getBroadcastId();
	
	void loadBroadcastListFromDatastore();

}
