package com.videobroadcast.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.videobroadcast.shared.IdNameTuple;

@RemoteServiceRelativePath("videobroadcastservice")
public interface VideoBroadcastService extends RemoteService {

	void g();
	
	void myUploads(String token);

	String createBroadcast(String token);
	
	String getBroadcastId(String id, String string);

	Boolean makeBroadcastLive(String token);

	boolean stopStreaming(String token);

	List<IdNameTuple> loadBroadcastListFromDatastore();

}
