package com.videobroadcast.client;

import java.util.List;
import java.util.logging.Logger;

import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HTML;
import com.videobroadcast.shared.AuthData;
import com.videobroadcast.shared.BroadcastInfo;
import com.videobroadcast.shared.ErrorCodes;
import com.videobroadcast.shared.IdNameTuple;
import com.videobroadcast.shared.MyConstants;

public class VideoBroadcastServiceClientImpl {

	private static final Logger log = Logger
			.getLogger(VideoBroadcastServiceClientImpl.class.getName());
	
	private final VideoBroadcastServiceAsync service = GWT.create(VideoBroadcastService.class);
	private final TVListServiceAsync service2 = GWT.create(TVListService.class);
	private View mainView;
	
	private String broadcastId;
	private String liveStreamId;
	private String selectedPiece;
	private String channelName;
	private String broadcastTitle;
	private String privacyStatus;
	private String format;
	private String lastChannelId = null;
	private String currentChannelId = "";
	private Timer minStreamTimer;
	private boolean minStreamTimeOver = false;
	private long liveEndTime;
	private boolean isCheckingStreamStatus = false;
	
	private Timer checkStreamStatusTimer = new Timer() {
		
		@Override
		public void run() {
			Window.alert("Oh no!");
		}
	};
	
//	private Timer checkStreamStatusTimer = new Timer() {
//		@Override
//		public void run() {
//			
//		}
//	};
	
//	private Timer liveEndTimeThread;
	
	private boolean isLastTimeTryingToMakeBroadcastLive = false; // If it is the last time and the server can't trigger the transition to "live" then there's
																 // no stream from Wirecast coming in and the user will get a message.
	
	public boolean isLive; // Is true as soon as last created broadcast has state "live"
	private boolean wikiPagesInstantiated = false;

	private String pieceToLink;

	
	public VideoBroadcastServiceClientImpl(String url) {
		ServiceDefTarget endpoint = (ServiceDefTarget) this.service;
		endpoint.setServiceEntryPoint(url);
		
		
//		liveEndTimeThread = new Timer() {
//			
//			@Override
//			public void run() {
//				// LiveEndTime (Max time user can be live) is over!
//				mainView.setMessageLabelText(new HTML("The maximum time of " + MyConstants.MAX_TIME_LIVE + " seconds you can be "
//						+ "live is over. You can continue recording for your Youtube video but your recording will be "
//						+ "deleted from IMSLP."), "messagePanel_small", false);
//			}
//		};
		
	}

	public void setView(View mainView) {
		this.mainView = mainView;
	}
	
	public View getView() {
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
	
	private class MakeBroadcastLiveCallback implements AsyncCallback<Long> {

		@Override
		public void onFailure(Throwable caught) {
			System.out.println("MakeBroadcastLive RPC: An error occured!");
			// TODO: Message?
			mainView.enableGoLiveButton(true);
			mainView.showGoLiveOverlay(false);
			mainView.setCursorWaiting(false);
			mainView.setLiveStatusLabel("offline");
			Window.alert("An unusual error occured. Please try again!");
		}
		@Override
		public void onSuccess(Long returnedLiveEndTime) {
			System.out.println("Response received!");
			if (returnedLiveEndTime != 0) {	// It is 0 if the transition to status "live" failed
				isLive = true;
//				liveEndTime = System.currentTimeMillis() + MyConstants.MAX_TIME_LIVE - 10000; // Subtract a few buffer seconds
				liveEndTime = returnedLiveEndTime;
				mainView.showGoLiveOverlay(false);
				mainView.setViewLive(true);
				mainView.setCursorWaiting(false);
//				mainView.setVideoId(myBroadcastId);
//				mainView.setTextBoxVideoId(myBroadcastId);
				mainView.setGoLiveButtonText("Stop!");
				mainView.enableGoLiveButton(false);
				mainView.enableChoosePieceButton(false);
				mainView.setLiveStatusLabel("LIVE!");
				HTML selectedPieceHTML = new HTML(selectedPiece);
				selectedPieceHTML.setStyleName("selectedPiece");
				mainView.setMessageLabelText(new HTML("Recording now! \n You are playing: </br>" + selectedPieceHTML),"messagePanel_medium", false); // Display will be updated within 30-60 seconds because of the Youtube live stream latency!
				mainView.setProgressBarDone();
				lastChannelId = currentChannelId;
				
				System.out.println("Success: MakeBroadcastLive RPC response: Broadcast is live!");
				GATracker.trackEvent("Live streaming flow", "Make broadcast live", "successful");
				
				minStreamTimeOver = false;
				
				// Let GoLive Button disabled for 5 seconds because it changed its function and to prevent stopping 
				// the Livestream accidently right after it is created by clicking the button several times in a short time 
				Timer t = new Timer() {
					@Override
					public void run() {
						mainView.enableGoLiveButton(true);
					}
				};
				t.schedule(5000);
				
				minStreamTimer = new Timer() {
					
					@Override
					public void run() {
						// Now the user can stop streaming and the video won't be deleted
						minStreamTimeOver = true;
					}
				};
				minStreamTimer.schedule(MyConstants.MIN_STREAM_TIME);
				
			} else {
				if (isLastTimeTryingToMakeBroadcastLive) {
					mainView.setLiveStatusLabel("offline");
					mainView.setMessageLabelText(new HTML("Error: Broadcast is not live! Stream not active yet? </br> Activate Stream via <a href=\"http://www.telestream.net/wirecastforyoutube/cb-landing.htm\" target=\"_blank_\"> Wirecast </a> and retry!"),"messagePanel_medium_padding_5", false);
					isLastTimeTryingToMakeBroadcastLive = false;
					mainView.enableChoosePieceButton(true);
					mainView.enableGoLiveButton(true);
				}
				System.out.println("Failure: MakeBroadcastLive RPC response: Broadcast is not live! Stream not active yet? Activate Stream and retry!");
			}
		}
	}

	public void checkStreamStatus(boolean startChecking) {
		
		if (startChecking) {
			
			AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
		    .withScopes(AuthData.SCOPE_YOUTUBE);
			
			Auth.get().login(req, new Callback<String, Throwable>() {

				@Override
				public void onFailure(Throwable reason) {
//					mainView.enableGoLiveButton(true);
				}

				@Override
				public void onSuccess(String token) {
					mainView.isAuthenticated = true;
					mainView.enableGoLiveButton(false);
						
					 service.getChannelId(token, new AsyncCallback<String>() {

							@Override
							public void onFailure(Throwable caught) {
							}

							@Override
							public void onSuccess(String result) {
								currentChannelId = result;
								
								 if (lastChannelId != null) {
									 if (!lastChannelId.equals(currentChannelId)) {
										 // User switched account!
//										 Window.alert("User switched account! Last: " + lastChannelId + "   current: " + currentChannelId);
//										 mainView.setIsBroadcastCreated(false);
//										 mainView.setStepTwoDoneAndKeep(false);
//										 mainView.enableChoosePieceButton(true);
//										 mainView.enableGoLiveButton(true);
										 Window.alert("Please note: You switched your account! You will have to login to the same account in Wirecast.");
										 deleteOldAndCreateNewBroadcast();
									 } else {
										 
											isCheckingStreamStatus = true;
											mainView.enableGoLiveButton(false);
											
											mainView.setLiveStatusLabel("waiting for stream");
											HTML selectedPieceHTML = new HTML(selectedPiece); // "\"" + titleOfPiece + "\""
											selectedPieceHTML.setStyleName("selectedPiece");
											mainView.setMessageLabelText(new HTML("Activate stream in <a href=\"http://www.telestream.net/wirecastforyoutube/cb-landing.htm\" target=\"_blank_\"> Wirecast </a> for the piece: </br>" 
																+ selectedPieceHTML + "You will be informed when the stream comes in (it takes 10 seconds)"),"messagePanel_small", false);
											
											final AsyncCallback<String> checkStreamStatusCallback = new AsyncCallback<String>() {
									
												@Override
												public void onFailure(Throwable caught) {
												}
									
												@Override
												public void onSuccess(final String streamStatus) {
															mainView.isAuthenticated = true;
															if (streamStatus.equals("active")) {
																checkStreamStatusTimer.cancel();
																isCheckingStreamStatus = false;
																mainView.showGoLiveOverlay(false);		// Maybe the success callback is triggered twice
																mainView.showGoLiveOverlay(true);
																mainView.setLiveStatusLabel("stream detected");
																HTML selectedPieceHTML = new HTML(selectedPiece); // "\"" + titleOfPiece + "\""
																selectedPieceHTML.setStyleName("selectedPiece");
																mainView.setMessageLabelText(new HTML("You are about to play: </br>" + selectedPieceHTML),"messagePanel_small", false);
															} else {
																checkStreamStatusTimer.schedule(6000);
															}									
													
												}
											};
											
											checkStreamStatusTimer = new Timer() {
												@Override
												public void run() {
																
																mainView.isAuthenticated = true;
																service.checkStreamStatus(liveStreamId, checkStreamStatusCallback);
												}
											};
											checkStreamStatusTimer.schedule(6000);
									 }
								 }
							}
					 });
					
					
				}
			});
			
		} else {
			checkStreamStatusTimer.cancel();
			isCheckingStreamStatus = false;
		}
		
	}
	
	public void deleteOldAndCreateNewBroadcast() {
			AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_YOUTUBE);
		
		Auth.get().login(req, new Callback<String, Throwable>() {
			  @Override
			  public void onSuccess(final String token) {
				  
				  mainView.isAuthenticated = true;
				  mainView.setCursorWaiting(true);
				  mainView.enableChoosePieceButton(false);
				  mainView.enableGoLiveButton(false);
//				  mainView.setChoosePieceOverlayControlsEnabled(false);
				  
				  // Get ChannelId
				 service.getChannelId(token, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						// TODO: Overlay nicht mehr anzeigen, Message ausgeben, Retry
					}

					@Override
					public void onSuccess(String result) {
						currentChannelId = result;
						
						 if (lastChannelId != null) {
							 if (!lastChannelId.equals(currentChannelId)) {
								 // User switched account!
//								 Window.alert("User switched account! Last: " + lastChannelId + "   current: " + currentChannelId);
								 mainView.setIsBroadcastCreated(false);
								 mainView.setStepTwoDoneAndKeep(false);
								 mainView.enableChoosePieceButton(true);
								 mainView.enableGoLiveButton(true);
							 }
						 }
						 
						 if (mainView.getIsBroadcastCreated()) {
							 /* User didn't switch account but chose another piece, so a new broadcast needs to be created
							 and the old one needs to be deleted. This can happen at the same time as the rest of this method. */
							 
							 // Delete old broadcast of current account just to keep account clean
							 	service.deleteBroadcastAndStream(token, broadcastId, liveStreamId, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										// TODO Auto-generated method stub
									}

									@Override
									public void onSuccess(Void result) {
										// TODO Auto-generated method stub
									}
								});
						 }
						 
						  /* CreateBroadcast-RPC: */
						  service.createBroadcast(token, selectedPiece, privacyStatus, format, new AsyncCallback<BroadcastInfo>() {
							@Override
							public void onFailure(Throwable caught) {
								mainView.setCursorWaiting(false);
								mainView.setMessageLabelText(new HTML("Connection error: Could not create Broadcast event in your Youtube account. Retry!"), "messagePanel_medium_padding_5", false);
								mainView.enableChoosePieceButton(true);
								mainView.enableGoLiveButton(true);
//								mainView.setChoosePieceOverlayControlsEnabled(true);
//								mainView.showChoosePieceOverlay(false);
								System.out.println("Failure: CreateBroadcast RPC failed");
							}
							@Override
							public void onSuccess(BroadcastInfo info) {
								
								mainView.setCursorWaiting(false);
								System.out.println("Success: CreateBroadcast RPC succeeded");
								if (!info.getErrorCode().equals(ErrorCodes.ERROR_LIVESTREAMING_NOT_ACTIVATED)) {
									/* Creating broadcast was successful! */
									broadcastId = info.getBroadcastId();
									liveStreamId = info.getLiveStreamId();
									channelName = info.getChannelName();
									broadcastTitle = info.getBroadcastTitle();
									liveEndTime = info.getLiveEndTime();
									lastChannelId = info.getChannelId();
									mainView.setBroadcastId(broadcastId);
									mainView.setStepTwoDoneAndKeep(true);
									HTML selectedPieceHTML = new HTML(selectedPiece); // "\"" + selectedPiece + "\""
									selectedPieceHTML.setStyleName("selectedPiece");
									mainView.setMessageLabelText(new HTML("You are about to play: </br>" + selectedPieceHTML + "Activate stream via Wirecast and click \"Go live!\" again"),"messagePanel_small", false);
//									mainView.showChoosePieceOverlay(false);
									mainView.setIsBroadcastCreated(true);
									mainView.enableChoosePieceButton(true);
//									mainView.enableGoLiveButton(true);
									checkStreamStatus(true);
									GATracker.trackEvent("Live streaming flow", "Create broadcast", "successful");
								} else if (info.getErrorCode().equals(ErrorCodes.ERROR_LIVESTREAMING_NOT_ACTIVATED)) {
									mainView.setMessageLabelText(new HTML("Error: Could not create Broadcast event! "
											+ "Click here or follow step 3 to activate Livestreaming in your Youtube account!"),"messagePanel_liveStreamingNotEnabled", true);
									mainView.setGalleryElementIndex(2);
									mainView.setIsBroadcastCreated(false);
									mainView.enableChoosePieceButton(true);
									mainView.enableGoLiveButton(true);
//									mainView.showChoosePieceOverlay(false);
									System.out.println("Error: Broadcast event is NOT created, livestreaming not enabled in account!");
									GATracker.trackEvent("Live streaming flow", "Create broadcast", "Livestreaming not enabled");
								} else {
									Window.alert("Sorry, a very unusual Youtube backend error occured. Please try again after the page has been reloaded!");
									Window.Location.reload();
								}
							}});
						
					}
				});
				 
				

			  }
			  @Override
			  public void onFailure(Throwable caught) {
				  mainView.enableChoosePieceButton(true);
				  mainView.enableGoLiveButton(true);
//				  mainView.setChoosePieceOverlayControlsEnabled(true);
				  System.out.println("Login failed, no createBroadcast RPC sent");
				  System.out.println(caught.getMessage());
			  }
		});
		
	}

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

	public void makeBroadcastLive() {
		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_YOUTUBE);
		
		Auth.get().login(req, new Callback<String, Throwable>() {
			  @Override
			  public void onSuccess(final String token) {
				  
				  mainView.isAuthenticated = true;
				  mainView.showGoLiveOverlay(false);
				  
				  // Check if account+channel is still the same since broadcast was created with a certain account+channel
				  service.getChannelId(token, new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							// TODO: Retry?!
						}

						@Override
						public void onSuccess(String result) {
							currentChannelId = result;
							
							 if (lastChannelId != null) {
								 if (!lastChannelId.equals(currentChannelId)) {
 
									 // User switched account!
									 // => Alles zurücksetzen (vll hat neuer Account nichtmal livestreaming aktiviert  
									 // => Annahme, das Account von vorne anfängt
//									 Window.alert("User switched account! Last: " + lastChannelId + "   current: " + currentChannelId);
									 mainView.setMessageLabelText(new HTML("You switched your account/channel! Make sure that you are logged "
									 		+ "into the same account and channel after you choose a piece and click \"Go live\"!"),"messagePanel_small", false);
									 mainView.setIsBroadcastCreated(false);
									 mainView.setStepTwoDoneAndKeep(false);
									 mainView.setGalleryElementIndex(0);
									 mainView.enableGoLiveButton(false);
									 lastChannelId = null;
									 
									 GATracker.trackEvent("Account", "Make broadcast live", "User switched account");
									 
								 } else {
									  /* User didn't switch account */
									 
									  isLastTimeTryingToMakeBroadcastLive = false;
									  
									  mainView.enableGoLiveButton(false);
									  mainView.enableChoosePieceButton(false);
									  mainView.setLiveStatusLabel("...");
									  mainView.setMessageLabelText(new HTML("You will be live in a few seconds!"),"messagePanel_medium_padding_15", false);
									  mainView.setCursorWaiting(true);
										
									  // wait a few seconds
									  Timer timer = new Timer() {
					  					@Override
										  public void run() {
					  						  mainView.setCursorWaiting(false);
											  if (!isLive) {	// Sobald Broadcast live ist, wird ein anderer Timer in ServiceClientImpl (MakeBroadcastLiveCallback) gestartet
					  							mainView.getGoLiveButton().setEnabled(true);
											  }
										  }	
									  };
									  timer.schedule(10000);
									  
									  // Try several times! Also the first try has an artificial delay because Youtube needs time to trigger the
									  // transition. The idea: Larger waiting times are better than an error message for the user.
									  Timer t = new Timer() {
											@Override
											public void run() {
												if (!isLive) {
													service.makeBroadcastLive(token, broadcastId, liveStreamId, currentChannelId, channelName,
															selectedPiece, broadcastTitle, privacyStatus,  new MakeBroadcastLiveCallback());

													GATracker.trackEvent("Live streaming flow", "Make broadcast live RPC sent");
													
													  Timer t = new Timer() {
															@Override
															public void run() {
																if (!isLive) {
																	service.makeBroadcastLive(token, broadcastId, liveStreamId, currentChannelId, channelName,
																			selectedPiece, broadcastTitle, privacyStatus,  new MakeBroadcastLiveCallback());
																	
																	Timer t = new Timer() {

																		@Override
																		public void run() {
																			if (!isLive) {
																				service.makeBroadcastLive(token, broadcastId, liveStreamId, currentChannelId, channelName, 
																						selectedPiece, broadcastTitle, privacyStatus,  new MakeBroadcastLiveCallback());
																				isLastTimeTryingToMakeBroadcastLive = true;
																			}
																		}
																	};
																	t.schedule(3000);
																	
																}
															}
														};
														t.schedule(3000);
												}
											}
										};
										t.schedule(3000);
								 }
							 }
							 
						}
				  });
				  
				    
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
			  public void onSuccess(final String token) {
				  
				  mainView.isAuthenticated = true;
				  
				  // Check if account+channel is still the same since user went live with a certain account+channel
				  service.getChannelId(token, new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							mainView.enableGoLiveButton(true);
							mainView.setCursorWaiting(false);
							// TODO: Retry?! If it doesn't work => FAQ!
						}

						@Override
						public void onSuccess(String result) {
							currentChannelId = result;
							
							 if (lastChannelId != null) {
								 if (!lastChannelId.equals(currentChannelId)) {
 
									 // User switched account!
									 // => Reset everything (vll hat neuer Account nichtmal livestreaming aktiviert  
									 // => Annahme, das Account von vorne anfängt
									 
									 Window.alert("Attention: You switched your Youtube account while live streaming in another account is running!"
									 		+ " The recording will be deleted from IMSLP (not from your Youtube account!)."
									 		+ " Please turn of the \"Stream\"-button in Wirecast and  " // follow the instructions in the FAQ to 
									 		+ " stop the stream in your Youtube account properly.");
									 mainView.setMessageLabelText(new HTML("You switched your account/channel! Make sure that you are logged "
									 		+ "into the same account and channel when you click \"Go live!\" and \"Stop!\"!"),"messagePanel_small", false);

									 isLive = false;
									 mainView.setCursorWaiting(false);
									 mainView.enableGoLiveButton(false);
									 mainView.setViewLive(false);
									 mainView.setGoLiveButtonText("Go live!");
									 mainView.setLiveStatusLabel("offline");
									 mainView.setStepTwoDoneAndKeep(false);
									 mainView.setGalleryElementIndex(0);
									 mainView.setIsBroadcastCreated(false);
									 mainView.enableChoosePieceButton(true);
									 lastChannelId = null;
									 
									 GATracker.trackEvent("Account", "Stop stream", "User switched account");
									 
								 } else {
									 // Normal case: User wants to stop stream with same account+channel
				  
									  // Broadcast event beenden
									  service.stopStreaming(token, broadcastId, liveEndTime, new AsyncCallback<Boolean>() {
										@Override
										public void onFailure(Throwable caught) {
											if (caught.getMessage().equals("MAX_TIME_LIVE exceeded")) {
													mainView.setMessageLabelText(new HTML("The maximum time of " + MyConstants.MAX_TIME_LIVE / 1000 / 60 + " minutes a broadcast can be live exceeded! "
															+ "The video will be deleted from IMSLP but it still exists in your Youtube account."),"messagePanel_small", false);
													isLive = false;
													mainView.enableGoLiveButton(false);
													mainView.setViewLive(false);
													mainView.setGoLiveButtonText("Go live!");
													mainView.setLiveStatusLabel("offline");
													mainView.setStepTwoDoneAndKeep(false);
													mainView.setGalleryElementIndex(3);
													mainView.setIsBroadcastCreated(false);
													mainView.enableChoosePieceButton(true);
													GATracker.trackEvent("Live streaming flow", "Stop stream", "MAX_TIME_LIVE exceeded");
											} else if (caught.getMessage().equals("Redundant transition")) {
												mainView.setMessageLabelText(new HTML("Error: Could not stop Broadcast event in your Youtube account because it was already stopped! The recording is deleted from IMSLP."),"messagePanel_medium", false);
												isLive = false;
												mainView.enableGoLiveButton(false);
												mainView.setViewLive(false);
												mainView.setGoLiveButtonText("Go live!");
												mainView.setLiveStatusLabel("offline");
												mainView.setStepTwoDoneAndKeep(false);
												mainView.setGalleryElementIndex(3);
												mainView.setIsBroadcastCreated(false);
												mainView.enableChoosePieceButton(true);
												GATracker.trackEvent("Live streaming flow", "Stop stream", "Broadcast event was already finished");
											} else if (caught.getMessage().equals("Video is already deleted from datastore")) {
												mainView.setMessageLabelText(new HTML("IMSLP can't reach your video anymore! </br> But it still exists in your account."),"messagePanel_medium", false);
												isLive = false;
												mainView.enableGoLiveButton(false);
												mainView.setViewLive(false);
												mainView.setGoLiveButtonText("Go live!");
												mainView.setLiveStatusLabel("offline");
												mainView.setStepTwoDoneAndKeep(false);
												mainView.setGalleryElementIndex(3);
												mainView.setIsBroadcastCreated(false);
												mainView.enableChoosePieceButton(true);
												GATracker.trackEvent("Live streaming flow", "Stop stream", "Cant't reach video anymore");
											} else {
												mainView.setMessageLabelText(new HTML("Error: Could not stop Broadcast event in your Youtube account. Retry!"),"messagePanel_medium", false);
												mainView.enableGoLiveButton(true);
											}
											mainView.setCursorWaiting(false);
											lastChannelId = null;
										}
										@Override
										public void onSuccess(Boolean isBroadcastLive) {
											System.out.println("Success: stopStreaming RPC succeeded");
											if (!isBroadcastLive) {		
												isLive = false;
												mainView.enableGoLiveButton(false);
												mainView.setViewLive(false);
												mainView.setGoLiveButtonText("Go live!");
												mainView.setLiveStatusLabel("offline");
												mainView.setStepTwoDoneAndKeep(false);
												mainView.setGalleryElementIndex(0);
												mainView.setIsBroadcastCreated(false);
												mainView.enableChoosePieceButton(true);
												mainView.setCursorWaiting(false);
												lastChannelId = null;
												System.out.println("Success: Broadcast event stopped");
											
												if (!minStreamTimeOver) {
													GATracker.trackEvent("Live streaming flow", "Stop stream", "Live streaming was too short");
													
													// User didn't stream long enough, so the broadcast will be deleted
													service.deleteBroadcastCompletely(token, broadcastId, liveStreamId, new AsyncCallback<Void>() {
														
														@Override
														public void onFailure(Throwable caught) {
															log.warning("Stop stream RPC failed!");
															mainView.setMessageLabelText(new HTML("Couldn't stop stream! (RPC-fail)"),"messagePanel_medium", false);
														}
												
														@Override
														public void onSuccess(Void result) {
															mainView.setMessageLabelText(new HTML("Your recording has been deleted because it wasn't long enough."
																	+ "</br> A live stream needs a minimum length of " + (MyConstants.MIN_STREAM_TIME / 1000) + 
																	" seconds."),"messagePanel_small_padding_10", false);
														}
												
													});
												} else {
													mainView.setMessageLabelText(new HTML("Streaming stopped now."),"messagePanel_medium_padding_15", false);
													GATracker.trackEvent("Live streaming flow", "Stop stream", "successful");
													mainView.showStopStreamOverlay(true);
												}
											}
										}
									});
								 }
							}
						}
				  });
			  }
						
			  @Override
			  public void onFailure(Throwable caught) {
				  System.out.println(caught.getMessage());
				  System.out.println("Login failed, no stopStreaming RPC sent");
			  }
		});

	}

	public void deleteBroadcastAndStream(final String broadcastId, final String liveStreamId) {

		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_YOUTUBE);
		
		Auth.get().login(req, new Callback<String, Throwable>() {

			@Override
			public void onFailure(Throwable reason) {
				
			}

			@Override
			public void onSuccess(String token) {
				service.deleteBroadcastAndStream(token, broadcastId, liveStreamId, new AsyncCallback<Void>() {
					
					@Override
					public void onFailure(Throwable caught) {
						System.out.println("deleteCurrentBroadcast failed!");
//						mainView.setMessageLabelText(new HTML("An error occured. Your video couldn't be deleted. You"
//								+ "can delete it in your Youtube account"), "messagePanel_medium", false);
					}

					@Override
					public void onSuccess(Void result) {
						System.out.println("deleteCurrentBroadcast succeeded!");
					}
				});
			}
		});
	}

	public void getWikiPages(final boolean isForLinkVideoOverlay, String text) {
		service.getWikiPages(text, new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				
			}

			@Override
			public void onSuccess(List<String> result) {
				mainView.setWikiPages(isForLinkVideoOverlay, result);
			}
		});
	}

	public void instantiateWikiPages(final String title) {
		service.instantiateWikiPages(title, new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(Boolean titleExists) {
				if (titleExists && !mainView.choosePieceButtonClicked) {
					/* Set piece given over url parameter */
					mainView.setIsBroadcastCreated(false);
					setSelectedPiece(title);
					setPrivacyStatus("unlisted");
					mainView.showSelectedPieceMessage(title);
					mainView.enableGoLiveButton(true);
				}
			}
		});
	}

	public void checkPiece(final boolean isForLinkPieceOverlay, String title) {
		service.checkPiece(title, new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(Boolean result) {
				mainView.showPieceAcceptedResult(isForLinkPieceOverlay, result);
			}
		});
	}

	public void doFinalPieceCheck(final boolean isForLinkPieceOverlay, String selectedPiece) {
		service.doFinalPieceCheck(selectedPiece, new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(Boolean result) {
				mainView.showFinalPieceCheckResult(isForLinkPieceOverlay, result);
			}
		});
	}

	public void deleteBroadcastFromDatastore(String broadcastId) {
		service.deleteBroadcastFromDatastore(broadcastId, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("deleteBroadcastFromDatastore(String broadcastId) failed!");
			}

			@Override
			public void onSuccess(Void result) {
				System.out.println("deleteBroadcastFromDatastore(String broadcastId) succeeded!");
			}
		});
	}

	public void setBroadcastStaysOnIMSLP(boolean stayOnIMSLP, String broadcastId) {
		service.setBroadcastStaysOnIMSLP(stayOnIMSLP, broadcastId, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("setBroadcastStaysOnIMSLP(boolean stayOnIMSLP, String broadcastId) failed!");
			}

			@Override
			public void onSuccess(Void result) {
				System.out.println("setBroadcastStaysOnIMSLP(boolean stayOnIMSLP, String broadcastId) succeeded!");
			}
		});
	}

	public void deleteBroadcastCompletely() {
		AuthRequest req = new AuthRequest(AuthData.AUTH_URL, AuthData.CLIENT_ID)
	    .withScopes(AuthData.SCOPE_YOUTUBE);
		
		Auth.get().login(req, new Callback<String, Throwable>() {

			@Override
			public void onFailure(Throwable reason) {
			}

			@Override
			public void onSuccess(String token) {
				service.deleteBroadcastCompletely(token, broadcastId, liveStreamId, new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Void result) {
						mainView.setMessageLabelText(new HTML("Your video has been deleted."), "messagePanel_medium_padding_15", false);
						mainView.showStopStreamOverlay(false);
					}
				});
				
			}});
		
		
	}

	public void setSelectedPiece(String selectedPiece) {
		this.selectedPiece = selectedPiece;
	}
	
	public void setPieceToLink(String pieceToLink) {
		this.pieceToLink = pieceToLink;
	}
	
	public String getSelectedPiece() {
		return this.selectedPiece;
	}

	public void setPrivacyStatus(String privacyStatus) {
		this.privacyStatus = privacyStatus;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getFormat() {
		return this.format;
	}

	public boolean getIsCheckingStreamStatus() {
		return isCheckingStreamStatus;
	}

	public void checkIfVideoIsAvailable(String videoId) {
		service.checkIfVideoIsAvailable(videoId, pieceToLink, new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("An error occured. Please reload page and try again.");
				mainView.setCursorWaiting(false);
			}

			@Override
			public void onSuccess(Boolean result) {
				mainView.setCursorWaiting(false);
				mainView.setLinkVideoButtonEnabled(true);
				if (result) {
					mainView.showLinkPieceSuccess();
					GATracker.trackEvent("Link video", "Video successfully linked");
				} else 
					Window.alert("Your video could not be linked with IMSLP because it's not accessible. "
						+ "Either the video doesn't exist or the privacy settings of the video are set to \"private\".");
				GATracker.trackEvent("Link video", "Video not linked because not accessible");
			}
		});
	}

}
	