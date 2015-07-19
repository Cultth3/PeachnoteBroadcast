package com.videobroadcast.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dev.shell.remoteui.MessageTransport.ErrorCallback;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.googlecode.objectify.annotation.Id;
import com.videobroadcast.shared.AuthData;
import com.videobroadcast.shared.ErrorCodes;
import com.videobroadcast.shared.IdNameTuple;

public class VideoBroadcastServiceClientImpl implements VideoBroadcastServiceClientInterface {

	private final VideoBroadcastServiceAsync service = GWT.create(VideoBroadcastService.class);
	private MainView mainView;
	
	private String myBroadcastId;
	
	public boolean isLive; // Is true as soon as last created broadcast has state "live"

	public VideoBroadcastServiceClientImpl(String url) {
		ServiceDefTarget endpoint = (ServiceDefTarget) this.service;
		endpoint.setServiceEntryPoint(url);

		this.mainView = new MainView(this);
	}

	public MainView getMainView() {
		return this.mainView;
	}

	private class DefaultCallback implements AsyncCallback {
		@Override
		public void onFailure(Throwable caught) {
//			System.out.println("RPC: An error occured!");
		}
		@Override
		public void onSuccess(Object result) {
//			System.out.println("RPC: Response received!");
		}
	}
	
	private class MakeBroadcastLiveCallback implements AsyncCallback {
		@Override
		public void onFailure(Throwable caught) {
			System.out.println("MakeBroadcastLive RPC: An error occured!");
		}
		@Override
		public void onSuccess(Object result) {
			System.out.println("Response received!");
			if ((Boolean) result) {
				isLive = true;
				mainView.setVideoId(myBroadcastId);
				mainView.setTextBoxVideoId(myBroadcastId);
				mainView.setGoLiveButtonText("Stop Stream!");
				mainView.enableGoLiveButton(false);
				mainView.setMessageLabelText("Recording now! Display will be updated within 30-60 seconds because of the Youtube live stream latency!", false);
				System.out.println("Success: MakeBroadcastLive RPC response: Broadcast is live!");
				
				// Let GoLive Button disabled for 3 seconds to prevent stopping the Livestream right after it is created by clicking the button several times fast
				// because it changed its function
				Timer t = new Timer() {
					@Override
					public void run() {
						mainView.enableGoLiveButton(true);
					}
				};
				t.schedule(3000);
					
			} else {
				mainView.setMessageLabelText("Error: Broadcast is not live! Stream not active yet? Activate Stream e.g. via Wirecast and retry!", false);
				System.out.println("Failure: MakeBroadcastLive RPC response: Broadcast is not live! Stream not active yet? Activate Stream and retry!");
			}
		}
	}

	@Override
	public void g() {
		this.service.g(new DefaultCallback());
	}

	@Override
	public void createBroadcast() {
		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_YOUTUBE);
		
		Auth.get().login(req, new Callback<String, Throwable>() {
			  @Override
			  public void onSuccess(String token) {
				  mainView.enableCreateLiveStreamButton(false);
				  System.out.println("Login succeeded, createBroadcast RPC sent");
				  service.createBroadcast(token, new AsyncCallback<String>() {
					@Override
					public void onFailure(Throwable caught) {
						mainView.enableLiveStreamButton(true);
						mainView.setMessageLabelText("Error: Could not create Broadcast event in your Youtube account! Livestream not enabled in your account?", false);
						System.out.println("Failure: CreateBroadcast RPC failed");
					}
					@Override
					public void onSuccess(String result) {
						
						System.out.println("Success: CreateBroadcast RPC succeeded");
						if (!result.equals(ErrorCodes.ERROR_LIVESTREAMING_NOT_ACTIVATED)) {
							myBroadcastId = result;
							mainView.enableGoLiveButton(true);
							mainView.setMessageLabelText("Broadcast event in your youtube account is created! Activate stream e.g. via Wirecast and click \"Go live!\"", false);
						} else {
//							mainView.setMessageLabelText("Error: Could not create Broadcast event! "
//									+ "Activate Livestreaming in your account!");
							
							mainView.setMessageLabelText("Error: Could not create Broadcast event! "
									+ "Click to activate Livestreaming in your account!", true);
							mainView.enableCreateLiveStreamButton(true);
							System.out.println("Error: Broadcast event is NOT created!");
						}
					}});

			  }
			  @Override
			  public void onFailure(Throwable caught) {
				  mainView.enableCreateLiveStreamButton(true);
				  System.out.println("Login failed, no createBroadcast RPC sent");
				  System.out.println(caught.getMessage());
			  }
		});
		
	}
	

	@Override
	public void myUploads() {
		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_READONLY);
		
		Auth.get().login(req, new Callback<String, Throwable>() {
			  @Override
			  public void onSuccess(String token) {
				  service.myUploads(token, new DefaultCallback());
				  System.out.println("Login succeeded, myUploads RPC sent");
			  }
			  @Override
			  public void onFailure(Throwable caught) {
				  System.out.println(caught.getMessage());
				  System.out.println("Login failed, no myUploads RPC sent");
			  }
		});
		
	}

	@Override
	public void getBroadcastId() {
//		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
//	    .withScopes(AuthData.SCOPE_READONLY);
//		
//		Auth.get().login(req, new Callback<String, Throwable>() {
//			  @Override
//			  public void onSuccess(String token) {
//			    // You now have the OAuth2 token needed to sign authenticated requests.
				  service.getBroadcastId("iwas", "blabla", new DefaultCallback());
//				  System.out.println("Login succeeded, getID RPC sent");
//			  }
//			  @Override
//			  public void onFailure(Throwable caught) {
//			    // The authentication process failed for some reason, see caught.getMessage()
//				  System.out.println(caught.getMessage());
//				  System.out.println("Login failed, no getID RPC sent");
//			  }
//		});
	}

	public void makeBroadcastLive() {
		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_YOUTUBE);
		
		Auth.get().login(req, new Callback<String, Throwable>() {
			  @Override
			  public void onSuccess(String token) {
				  service.makeBroadcastLive(token, new MakeBroadcastLiveCallback());
				  System.out.println("Login succeeded, makeBroadcastLive RPC sent");
			  }
			  @Override
			  public void onFailure(Throwable caught) {
				  System.out.println(caught.getMessage());
				  System.out.println("Login failed, no makeBroadcastLive RPC sent");
			  }
		});
		
	}

	public void stopStreaming() {
		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_YOUTUBE);
		
		Auth.get().login(req, new Callback<String, Throwable>() {
			  @Override
			  public void onSuccess(String token) {
				  
				  // Broadcast event beenden
				  service.stopStreaming(token, new AsyncCallback() {
					@Override
					public void onFailure(Throwable caught) {
						System.out.println("Failure: stopStreaming RPC failed");
						mainView.setMessageLabelText("Error: Could not stop Broadcast event in your Youtube account! (RPC failed)", false);
					}
					@Override
					public void onSuccess(Object isBroadcastLive) {
						System.out.println("Success: stopStreaming RPC succeeded");
						if (!(Boolean) isBroadcastLive) {
							isLive = false;
							mainView.enableGoLiveButton(false);
							mainView.enableLiveStreamButton(true);
							mainView.setGoLiveButtonText("Go live!");
							mainView.setMessageLabelText("Streaming stopped now. The broadcast event in your account is deleted.", false);
							mainView.setVideoId(myBroadcastId);
							System.out.println("Success: Broadcast event stopped and event is deleted");
						}
					}
				});
				  
				  System.out.println("Login succeeded, stopStreaming RPC sent");
			  }
			  @Override
			  public void onFailure(Throwable caught) {
				  System.out.println(caught.getMessage());
				  System.out.println("Login failed, no stopStreaming RPC sent");
			  }
		});

	}

	@Override
	public void loadBroadcastListFromDatastore() {
		service.loadBroadcastListFromDatastore(new AsyncCallback() {
			@Override
			public void onFailure(Throwable caught) {
				System.out.println("Loading BroadcastList failed!");
			}

			@Override
			public void onSuccess(Object result) {
				System.out.println("Loading BroadcastList was successful!");

				// TODO: IdNameTuple auflösen
				
				List<IdNameTuple> broadcastList = (ArrayList<IdNameTuple>) result;
				
				mainView.updateBroadcastList(broadcastList);
			}
			
		});
	}

}
	