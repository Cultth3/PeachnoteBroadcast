package com.videobroadcast.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This overlay pops up after stopping a livestream The user can choose if he
 * wants to keep the video on IMSLP and in his Youtube account, if he wants to
 * delete it from IMSLP but keep it in his Youtube account or if he wants to
 * delete the video completely
 * 
 * @author Tom
 *
 */

public class StopStreamOverlay extends Composite {

	private VerticalPanel overlay = new VerticalPanel();
	private VerticalPanel mainPanel = new VerticalPanel();
	private VerticalPanel titlePanel = new VerticalPanel();
	private VerticalPanel radioButtonPanel = new VerticalPanel();

	private Button cancelButton;
	private Button okButton;
	private String selectedPiece;
	private String broadcastId;
	
	public StopStreamOverlay(final View mainView, final VideoBroadcastServiceClientImpl serviceImpl, String selectedPiece, String broadcastId) {
		initWidget(overlay);
		
		setStyleName("overlay");	

		this.selectedPiece = selectedPiece;
		this.broadcastId = broadcastId;
		
		mainPanel.setStyleName("overlayMainPanel");

//		final String title = param.replaceAll(" ", "%");
		String selectedPieceEncoded = URL.encodeQueryString(selectedPiece).replace("+", "_");;
		String imslpLink = "//imslp.org/wiki/" + selectedPieceEncoded;
		
		Label titleLabel = new Label("Congratulations!");
		titleLabel.setStyleName("stopStreamOverlayTitleLabel");
		HTML subTitle = new HTML("You were live with your interpretation of </br> <a class=\"overlaySelectedPiece\" href=\""+ imslpLink + "\" target=\"_blank\" style=\"text-decoration: underline; font-size: 1.2em;\"> " +  this.selectedPiece + " </a> </br>  <span class=\"stopStreamOverlayLinkHint\"> (Click the link to watch your performance on IMSLP.org) </span>"
									+ "</br> </br> Now you can choose if and where you want to keep your video");
		subTitle.setStyleName("stopStreamOverlaySubTitle");
		
		this.titlePanel.add(titleLabel);
		this.titlePanel.add(subTitle);
		
		final RadioButton standardRadioButton = new RadioButton("radioButtons","I think my performance was good. I want to keep my video in my Youtube account and on IMSLP.org.");
		final RadioButton deleteRadioButton = new RadioButton("radioButtons","I think my performance was not good. I want to delete my video completely.");
		final RadioButton partlyDeleteRadioButton = new RadioButton("radioButtons","I want to delete my video from IMSLP.org (and from this page) but I want to keep it in my Youtube account for myself.");

		radioButtonPanel.add(standardRadioButton);
		radioButtonPanel.add(deleteRadioButton);
		radioButtonPanel.add(partlyDeleteRadioButton);
		
		standardRadioButton.setValue(true);
		
		radioButtonPanel.getElement().setId("stopStreamOverlayRadioButtonPanel");
		
		
		cancelButton = new Button("Cancel");
		okButton = new Button("Ok");
		cancelButton.setStyleName("overlayButton");
		okButton.setStyleName("overlayButton");
		
		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.getElement().setId("stopStreamOverlayButtonPanel");
		buttonPanel.add(okButton);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mainView.showStopStreamOverlay(false);
			}		
		});
		
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (standardRadioButton.getValue()) {
					mainView.setBroadcastStaysOnIMSLP(true, StopStreamOverlay.this.broadcastId);
					mainView.showStopStreamOverlay(false);
					mainView.setMessageLabelText(new HTML("Streaming stopped. Your video stays on IMSLP.org and in the TV section of this website "
							+ "as long as it is available in your account."),"messagePanel_small_padding_10", false);
					GATracker.trackEvent("Live streaming flow", "Stop stream overlay", "Keep broadcast");
				} else if (deleteRadioButton.getValue()) {
					
					GATracker.trackEvent("Live streaming flow", "Stop stream overlay", "Delete broadcast completely");
					
					mainView.isAuthenticated = false;
					mainView.setStopStreamOverlayButtonsEnabled(false);
					
					mainView.deleteCompletedBroadcast();
					
					Timer t = new Timer() {
						
						@Override
						public void run() {
							if (!mainView.isAuthenticated)
								mainView.setStopStreamOverlayButtonsEnabled(true);
						}
					};
					t.schedule(3000);	
					
				} else if (partlyDeleteRadioButton.getValue()) {
					GATracker.trackEvent("Live streaming flow", "Stop stream overlay", "Delete broadcast from IMSLP");
					mainView.deleteBroadcastFromDatastore(StopStreamOverlay.this.broadcastId);
					mainView.showStopStreamOverlay(false);
				}
				
				
			}
		});
		
		okButton.setStyleName("overlayButton");
		
		mainPanel.add(titlePanel);
		mainPanel.add(radioButtonPanel);
		mainPanel.add(buttonPanel);
		
		overlay.add(this.mainPanel);

	}

	public Button getOkButton() {
		return okButton;
	}
	
}
