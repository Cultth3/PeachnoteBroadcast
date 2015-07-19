package com.videobroadcast.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */

public class VideoBroadcastEntryPoint implements EntryPoint {

	public void onModuleLoad() {
		
		VideoBroadcastServiceClientImpl clientImpl = 
				new VideoBroadcastServiceClientImpl(GWT.getModuleBaseURL() + "videobroadcastservice");
		TVListServiceClientImpl tvListClientImpl = 
				new TVListServiceClientImpl(GWT.getModuleBaseURL() + "tvsectionvideolistservletremote");
		
		View mainView = new View(clientImpl, tvListClientImpl);
		clientImpl.setView(mainView);
		tvListClientImpl.setView(mainView);
		
		RootPanel.get().add(mainView);
		
	}
}
