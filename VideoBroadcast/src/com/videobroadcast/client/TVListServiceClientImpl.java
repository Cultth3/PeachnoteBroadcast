package com.videobroadcast.client;

import java.util.List;

import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.videobroadcast.shared.AuthData;
import com.videobroadcast.shared.BroadcastInfo;

/**
 * A second RemoteService client only responsible for requests to
 * TVListServiceImpl
 * 
 * @author Tom
 *
 */

public class TVListServiceClientImpl {

	private final TVListServiceAsync tvListService = GWT.create(TVListService.class);
	private View mainView;
	private TVSectionListGallery tvSectionListGallery;
	
	public TVListServiceClientImpl(String url) {
		ServiceDefTarget endpoint = (ServiceDefTarget) this.tvListService;
		endpoint.setServiceEntryPoint(url);
	}

	public void setView(View mainView) {
		this.mainView = mainView;
	}
	
	public void setTVSectionListGallery(TVSectionListGallery gallery) {
		this.tvSectionListGallery = gallery;
	}
	
	public void requestVideoList(String searchTerm) {
		
		tvListService.giveVideoList(searchTerm, new AsyncCallback<List<BroadcastInfo>>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(List<BroadcastInfo> result) {
				tvSectionListGallery.setIndex(0);
				tvSectionListGallery.setBroadcastInfoList(result);
				tvSectionListGallery.updateGalleryElements();
			}
		});
	}

	public void deleteVideo(final String broadcastId) {

		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_READONLY);
		
		Auth.get().login(req, new Callback<String, Throwable>() {
			  @Override
			  public void onSuccess(String token) {
				  mainView.isAuthenticated = true;
				  mainView.setCursorWaiting(true);
				  
				  tvListService.deleteVideo(token, broadcastId, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(String result) {
						mainView.setCursorWaiting(false);
						mainView.hideDeleteVideoOverlay();
						
						if (result.equals("no permission")) {
							Window.alert("You are not allowed to delete this video! You can only delete "
									+ "videos of your own channel. ");
							GATracker.trackEvent("Account", "Delete video", "Not allowed");
						} else if (result.equals("deletion succeeded")){
							tvSectionListGallery.setSearchBoxText("");
							tvSectionListGallery.requestVideoList();
							GATracker.trackEvent("Account", "Delete video", "successful");
						} else {	
							Window.alert("The video is already deleted.");
							tvSectionListGallery.requestVideoList();
							GATracker.trackEvent("Account", "Delete video", "Already deleted");
						}
					}
				});
			  }
			  @Override
			  public void onFailure(Throwable caught) {
				  System.out.println(caught.getMessage());
			  }
		});
		
	}
	
	
}
	