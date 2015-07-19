package com.videobroadcast.client;

import java.util.Date;

import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.videobroadcast.shared.AuthData;


public class TVSectionListGalleryElement extends Composite {
	
	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel thumbnailAndControlsPanel = new HorizontalPanel();
	private FocusPanel thumbnailWrapper = new FocusPanel();
	private VerticalPanel titlePanel = new VerticalPanel();
	private VerticalPanel controlsPanel = new VerticalPanel();
	private VerticalPanel channelPanel = new VerticalPanel();
	private FocusPanel trashIconPanel = new FocusPanel();
	private HTML titleHTML = new HTML();
	private HTML channelNameHTML = new HTML();
//	private Image trashIcon = new Image(Resources.INSTANCE.iconsTrash().getSafeUri());
	private Image trashIcon = new Image("http://i.imgur.com/vpyd0bP.png");
	private View mainView;
	private TVListServiceClientImpl tvServiceImpl;

	public TVSectionListGalleryElement(View mainView, TVListServiceClientImpl tvServiceImpl) {
		initWidget(this.mainPanel);
		this.mainView = mainView;
		this.tvServiceImpl = tvServiceImpl;
		
//		String videoId = "h4rZR8rsRSY";
		
		trashIcon.setPixelSize(12, 16);
		
		thumbnailAndControlsPanel.setStyleName("tvSectionListGalleryElementThumbnailAndControlsPanel");
		thumbnailWrapper.setStyleName("tvSectionGalleryThumbnailWrapperDeactivated");
		controlsPanel.setStyleName("tvSectionListGalleryElementControlsPanel");
		titlePanel.setStyleName("tvSectionListGalleryElementTitlePanel");
		channelPanel.setStyleName("tvSectionListGalleryElementChannelPanel");
		
		thumbnailAndControlsPanel.add(thumbnailWrapper);
		controlsPanel.add(trashIconPanel);
		thumbnailAndControlsPanel.add(controlsPanel);
		mainPanel.add(thumbnailAndControlsPanel);
		mainPanel.add(titlePanel);
		mainPanel.add(channelPanel);
		
	}
	
	public void updateContent(final String broadcastId, final String piece, final String channelName, String lifeCycleStatus, String channelId) {
		
		if (piece.length() > 58) {
			final String shortBroadcastTitle = piece.substring(0, 54) + " ...";
			this.titleHTML = new HTML(shortBroadcastTitle);
			
			titleHTML.addDomHandler(new MouseOverHandler() {
				
				@Override
				public void onMouseOver(MouseOverEvent event) {
					titleHTML.setHTML(piece);
					channelPanel.setVisible(false);
				}
			}, MouseOverEvent.getType());
			
			titleHTML.addDomHandler(new MouseOutHandler() {
				
				@Override
				public void onMouseOut(MouseOutEvent event) {
					titleHTML.setHTML(shortBroadcastTitle);
					channelPanel.setVisible(true);
				}
			}, MouseOutEvent.getType());
			
		} else {
			this.titleHTML = new HTML(piece);
		}
		
		if (channelName.length() > 34) {
			final String shortChannelName = channelName.substring(0, 30) + " ...";
			this.channelNameHTML = new HTML(shortChannelName);
		} else {
			this.channelNameHTML = new HTML(channelName);
		}
		
		
		clearContent();
		thumbnailWrapper.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mainView.setVideoId(broadcastId);
			}
		});
		
		trashIconPanel = new FocusPanel();
		trashIconPanel.setStyleName("trashIconPanel");
		trashIconPanel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
			    .withScopes(AuthData.SCOPE_READONLY);
				
				Auth.get().login(req, new Callback<String, Throwable>() {
					  @Override
					  public void onSuccess(String token) {
						  mainView.showDeleteVideoOverlay(tvServiceImpl, piece, broadcastId);
					  }
					  
					  @Override
					  public void onFailure(Throwable caught) {
						  System.out.println(caught.getMessage());
					  }
				});
			}
		});
		trashIconPanel.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				trashIcon.setPixelSize(15, 20);
				
			}
		});
		trashIconPanel.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				trashIcon.setPixelSize(12, 16);
			}
		});
		if (lifeCycleStatus.equals("live")) {
			thumbnailWrapper.setStyleName("tvSectionGalleryThumbnailWrapperLive");
//			thumbnailWrapper.add(new HTML("<div style=\"width: 120px; height: 67px; overflow: hidden;\"> <img style=\"margin-top: " + -12 + "px; width: 120px; height: 90px;\" src=\"images/DefaultThumbnail.jpg\"> </img> </div>"));
//			channelPanel.add(new HTML("<span style=\"color: red;\"> played live by </span> <br>" + channelNameHTML + ""));
			final HorizontalPanel liveOverlay = new HorizontalPanel();
			liveOverlay.setStyleName("tvSectionGalleryThumbnailWrapperLiveOverlay");
			liveOverlay.addStyleName("blinkingSmoothly");
			liveOverlay.add(new HTML("<div style=\"text-align: center; max-height: 20px; font-size: 1.2em;  vertical-align: middle;\"> live! </div>"));
			Timer t = new Timer() {
				@Override
				public void run() {
					thumbnailWrapper.add(liveOverlay);
				}
			};
			t.schedule(500);
			
		} else {
			thumbnailWrapper.add(new HTML("<div style=\"width: 120px; height: 67px; overflow: hidden;\"> <img style=\"margin-top: " + -12 + "px; width: 120px; height: 90px;\" src=\"//img.youtube.com/vi/" + broadcastId + "/default.jpg\"> </img> </div>"));
		}

		channelPanel.add(new HTML("<span style=\"color: #696969;\"> played by </span> <a href=\"https://www.youtube.com/channel/" + channelId + "\" target=\"_blank_\" style=\"margin-left: 2px;\">" + channelNameHTML + "</a>")); //color: #262626;
		controlsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		controlsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		trashIconPanel.add(trashIcon);
		FocusPanel imslpLinkPanel = new FocusPanel();
		imslpLinkPanel.setStyleName("tvGalleryElementImslpLink");
		imslpLinkPanel.setTitle("Watch this interpretation on IMSLP.org");
		imslpLinkPanel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String titleEncoded = URL.encodeQueryString(piece).replace("+", "_");;
				String imslpLink = "//imslp.org/wiki/" + titleEncoded;
				Window.open(imslpLink, "_blank", "");
			}
		});
		controlsPanel.add(imslpLinkPanel);
		controlsPanel.add(trashIconPanel);
		thumbnailAndControlsPanel.add(thumbnailWrapper);
		thumbnailAndControlsPanel.add(controlsPanel);
		titlePanel.add(titleHTML);
		
		/* Tooltips */
		trashIconPanel.setTitle("Delete video");
		thumbnailWrapper.setTitle(piece + " played by " + channelName);
	}

	public void clearContent() {
		thumbnailAndControlsPanel.clear();
		thumbnailWrapper.clear();
		controlsPanel.clear();		
		titlePanel.clear();
		channelPanel.clear();
	}
	
	public void activateGalleryElement(boolean activate) {
		if (activate)
			thumbnailWrapper.setStyleName("tvSectionGalleryThumbnailWrapper");
		else
			thumbnailWrapper.setStyleName("tvSectionGalleryThumbnailWrapperDeactivated");
	}

	
	
	public static String toLocaleDateString(Date d) {
	      if (GWT.isScript()) {
	         return nativeToLocaleDateString(d);
	      } else {
	    	  return "uiniu";
	         // put your hosted-mode "pure Java" code here
	      }
	   } 
	
	
	public static native String nativeToLocaleDateString(Date d) /*-{
    return d.jsdate.toLocaleDateString();
 }-*/; 
	
	private native static String formatDate(int d) 
		/*-{    

			now = Date.now();
		    var diffMs = now - d;
		    var diffS = diffMs/1000;
		    var diffMn = diffS/60;
		    var diffHs = diffMn/60;
		    var diffDs = diffHs/24;
		    if (diffDs > 7) {
		        return date.toLocaleDateString();
		    }
		    			alert(d);
		    
		    if (diffHs > 22) {
		        var r = Math.round(diffDs);
		        return r + " day"+(r > 1 ? "s":"")+" ago";
		    }
		    if (diffMn > 50) {
		        var r = Math.round(diffHs);
		        return r + " hour"+(r > 1 ? "s":"")+" ago";
		    }
		    if (diffS > 50) {
		        var r = Math.round(diffMn);
		        return r + " minute"+(r > 1 ? "s":"")+" ago";
		    }
		    if (diffS > 10) {
		        var r = Math.round(diffS);
		        return r + " second"+(r > 1 ? "s":"")+" ago";
		    }
		    return "just now";
		
		}-*/;
		
	
}
