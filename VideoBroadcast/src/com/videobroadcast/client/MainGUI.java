package com.videobroadcast.client;

import open.pandurang.gwt.youtube.client.PlayerConfiguration;
import open.pandurang.gwt.youtube.client.YouTubePlayer;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainGUI extends Composite {

	private VideoBroadcastServiceClientImpl serviceImpl;
	private VerticalPanel contentPanel;

	public MainGUI(VideoBroadcastServiceClientImpl serviceImpl) {
		contentPanel = new VerticalPanel();
		initWidget(contentPanel);
		
		this.serviceImpl = serviceImpl;
		
		MainView testView = new MainView(serviceImpl);
		contentPanel.add(testView);
	}
	
}
