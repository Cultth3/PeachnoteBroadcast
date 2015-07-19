package com.videobroadcast.client;

import java.awt.TextField;
import java.util.Iterator;
import java.util.List;

import javax.swing.RootPaneContainer;

import open.pandurang.gwt.youtube.client.ApiReadyEvent;
import open.pandurang.gwt.youtube.client.ApiReadyEventHandler;
import open.pandurang.gwt.youtube.client.PlayerConfiguration;
import open.pandurang.gwt.youtube.client.PlayerReadyEvent;
import open.pandurang.gwt.youtube.client.PlayerReadyEventHandler;
import open.pandurang.gwt.youtube.client.StateChangeEvent;
import open.pandurang.gwt.youtube.client.StateChangeEventHandler;
import open.pandurang.gwt.youtube.client.YouTubePlayer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.videobroadcast.shared.IdNameTuple;

public class MainView extends Composite {

	private static final String PLAYER_HEIGHT = "360";
	private static final String PLAYER_WIDTH = "640";
	
	private PlayerConfiguration config;
	private YouTubePlayer player;
	private VerticalPanel vPanel = new VerticalPanel();
	private HorizontalPanel buttonPanel;
	private VerticalPanel labelPanel;
	private VerticalPanel contentPanel;
	private VerticalPanel embeddedPlayerPanel;
	private HorizontalPanel embeddedPlayerControlPanel;
	private VerticalPanel liveStreamListPanel;
	private VerticalPanel listPanel;
	private Button changeVideoButton;
	private Button goLiveButton;
	private Button myUploadsButton;
	private Button createLiveStreamButton;
	private Button refreshPlayerButton;
	private Button refreshBroadcastListButton;
	private TextBox textBoxVideoId;
	private TextBox textBoxCommands;
	private Label messageLabel;
	private Anchor messageAnchor;
	
	VideoBroadcastServiceClientImpl serviceImpl;
	
	public MainView(final VideoBroadcastServiceClientImpl serviceImpl) {
		initWidget(this.vPanel);
		this.contentPanel = new VerticalPanel();
		this.embeddedPlayerPanel = new VerticalPanel();
		this.embeddedPlayerControlPanel = new HorizontalPanel();
		this.listPanel = new VerticalPanel();
		this.liveStreamListPanel = new VerticalPanel();
		this.buttonPanel = new HorizontalPanel();
		this.labelPanel = new VerticalPanel();
		this.vPanel.add(contentPanel);
		this.embeddedPlayerPanel.add(embeddedPlayerControlPanel);
		
		this.goLiveButton = new Button("Go live!");
		this.goLiveButton.setEnabled(false);
		this.changeVideoButton = new Button("Change Video");
		this.myUploadsButton = new Button("My Uploads");
		this.createLiveStreamButton = new Button("Create Live Stream");
		this.refreshPlayerButton = new Button("Refresh");
		this.refreshBroadcastListButton = new Button("Refresh List");
		
		Label titleLabel = new Label("Music Live Broadcasting");
		textBoxVideoId = new TextBox();
		textBoxVideoId.setText("(Insert Video ID)");
		textBoxCommands = new TextBox();
		messageLabel = new Label();
		messageAnchor = new Anchor();

		this.buttonPanel.add(createLiveStreamButton);
		this.buttonPanel.add(goLiveButton);
//		this.buttonPanel.add(changeVideoButton);
//		this.buttonPanel.add(myUploadsButton);
		this.embeddedPlayerControlPanel.add(textBoxVideoId);
		this.embeddedPlayerControlPanel.add(refreshPlayerButton);
		this.listPanel.add(refreshBroadcastListButton);
		this.listPanel.add(liveStreamListPanel);
		
		this.contentPanel.add(titleLabel);
		this.contentPanel.add(buttonPanel);
		this.contentPanel.add(labelPanel);
//		this.contentPanel.add(textBoxCommands);
		
		RootPanel.get("contentPanelContainer").add(contentPanel);
		RootPanel.get("embeddedPlayerContainer").add(embeddedPlayerPanel);
		RootPanel.get("liveStreamListContainer").add(listPanel);
		
		titleLabel.addStyleName("title");
		this.listPanel.setSpacing(10);
		this.liveStreamListPanel.addStyleName("liveStreamListPanel");
		this.contentPanel.addStyleName("contentPanel");
		this.buttonPanel.setSpacing(5);
		this.buttonPanel.addStyleName("buttonPanel");
		this.labelPanel.addStyleName("labelPanel");
		this.labelPanel.setSpacing(10);
		
		this.createLiveStreamButton.addClickHandler(new CreateLiveStreamButtonHandler());
		this.goLiveButton.addClickHandler(new GoLiveButtonHandler());
		this.changeVideoButton.addClickHandler(new ChangeVideoButtonHandler());
		this.myUploadsButton.addClickHandler(new MyUploadsButtonHandler());
		this.textBoxVideoId.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					setVideoId(textBoxVideoId.getText());
					if (textBoxVideoId.getText().equals("test"))
						serviceImpl.getBroadcastId();
					if (textBoxVideoId.getText().equals("load"))	
						serviceImpl.loadBroadcastListFromDatastore();		
				}
			}
		});
		this.refreshPlayerButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setVideoId(textBoxVideoId.getText());
				}
		});
		this.refreshBroadcastListButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				createBroadcastList();
			}
		});
		this.textBoxCommands.addKeyDownHandler(new TextBoxCommandChangeHandler());
		
		
		this.serviceImpl = serviceImpl;
		createAndAddYouTubePlayer();
		createBroadcastList();
	}

	private void createAndAddYouTubePlayer() {
		YouTubePlayer.loadYouTubeIframeApi();
        YouTubePlayer.addApiReadyHandler(new ApiReadyEventHandler() {
			@Override
			public void onApiReady(ApiReadyEvent event) {
				createYouTubePlayer();
			}
        });
	}
	
	private void createYouTubePlayer() {
		
		this.config = (PlayerConfiguration) PlayerConfiguration.createObject();
        config.setVideoId("O6Cn2CG3PBY");
        config.setHeight(PLAYER_HEIGHT);
        config.setWidth(PLAYER_WIDTH);
        
        this.player = new YouTubePlayer(config);
        this.player.addPlayerReadyHandler(new PlayerReadyEventHandler() {

            public void onPlayerReady(PlayerReadyEvent event) {
                GWT.log("First player is ready.");
                GWT.log("First player state -> " + player.getPlayer().getPlayerState());
            }
        });
        this.player.addStateChangedHandler(new StateChangeEventHandler() {

            public void onStateChange(StateChangeEvent event) {
                GWT.log("First player state changed => " + event.getPlayerEvent().getData());
            }
        });
        
        this.embeddedPlayerPanel.add(this.player);

	}

	

	private class CreateLiveStreamButtonHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
//			createLiveStreamButton.setEnabled(false);
			serviceImpl.createBroadcast();
		}
	}
	
	private class GoLiveButtonHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			if (!serviceImpl.isLive) {
				goLiveButton.setEnabled(false);
				serviceImpl.makeBroadcastLive();
				
				// wait 4 seconds
				Timer t = new Timer() {
					@Override
					public void run() {
						if (!serviceImpl.isLive)	// Sobald Broadcast live ist, wird ein anderer Timer in ServiceClientImpl (MakeBroadcastLiveCallback) gestartet
							goLiveButton.setEnabled(true);
					}
				};
				t.schedule(4000);
				
			} else {
				serviceImpl.stopStreaming();
			}
		}
	}
	
	private class ChangeVideoButtonHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			setVideoId("xPp-eeTU6x0");
		}
	}

	private class MyUploadsButtonHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			serviceImpl.myUploads();
		}
	}
	
	
	private class TextBoxCommandChangeHandler implements KeyDownHandler {
		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				String command = textBoxCommands.getText();
				if (command.equals("golive")) 
					serviceImpl.createBroadcast();
				else if (command.equals("myuploads"))
					serviceImpl.myUploads();
				else if (command.equals("getid"))
					serviceImpl.getBroadcastId();
			}
		}
	}
	
	public void setVideoId(String id) {
		player.getPlayer().loadVideoById(id);
	}
	
	private void createBroadcastList() { // später: Parameter List o.ä.
		this.liveStreamListPanel.clear();
		this.serviceImpl.loadBroadcastListFromDatastore();
		
//		final String[] array = {"ylLzyHk54Z0", "tVIIgcIqoPw", "I8xZBfVsMzs"};
//		Timer t = new Timer() {
//			@Override
//			public void run() {
//
//				Iterator<String> iterator = broadcastList.iterator();
//				
//				int i = 1;
//				
//				while (iterator.hasNext()) {
//					final String broadcastId = iterator.next();
//					
//					final Anchor label = new Anchor("Live Stream " + i);
//					label.setStyleName("LiveStreamListLabel");
//					label.addClickHandler(new ClickHandler() {
//						@Override
//						public void onClick(ClickEvent event) {
//							setVideoId(broadcastId);
//						}
//					});
//					
//					liveStreamListPanel.add(label);
//					i++;
//				}
//			
//			}
//		};
//		t.schedule(5000);
//		
	}
			
	public void updateBroadcastList(List<IdNameTuple> broadcastList) {
		
		// TODO: For the implementation in IMSLP it is important to know which piece the user wants to play. So the origin of the Live Stream activate button 
		// is important for telling the list the name of the piece and giving its corresponding anchor the right name.
		
		int i = 1;
		
		for (IdNameTuple x:broadcastList) {
			final String channelName = x.channelName;
			final String id = x.id;
			
			final Anchor anchorName = new Anchor("Live Stream " + i + " (" + channelName + ")");
			anchorName.setStyleName("LiveStreamListLabel");
			anchorName.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setVideoId(id);
				}
			});
			
			liveStreamListPanel.add(anchorName);
			i++;
		}
	}
	
	public void enableGoLiveButton(boolean enabled) {
		this.goLiveButton.setEnabled(enabled);
	}
	
	public void enableLiveStreamButton(boolean enabled) {
		this.createLiveStreamButton.setEnabled(enabled);
	}

	public void setGoLiveButtonText(String text) {
		this.goLiveButton.setText(text);
	}
	
	public void setMessageLabelText(String message, boolean isAnchor) {
		
		if (isAnchor) {
			this.labelPanel.remove(this.messageLabel);
			this.labelPanel.clear();
			
			this.messageAnchor = new Anchor(message);
			this.messageAnchor.setStyleName("messageAnchor");
			this.messageAnchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Window.open("https://www.youtube.com/my_live_events","_blank","");
				}
			});
			this.labelPanel.add(this.messageAnchor);
			
		} else {
			
			this.labelPanel.remove(this.messageAnchor);
			this.labelPanel.clear();
			this.messageLabel = new Label();
			this.messageLabel.setStyleName("messageLabel");
			this.messageLabel.setText(message);
			this.labelPanel.add(this.messageLabel);
			
		}
		
	}
	
	public void setTextBoxVideoId(String id) {
		this.textBoxVideoId.setText(id);
	}
	
	public void enableCreateLiveStreamButton(boolean enabled) {
		this.createLiveStreamButton.setEnabled(enabled);
	}

}	
