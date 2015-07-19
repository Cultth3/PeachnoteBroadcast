package com.videobroadcast.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.videobroadcast.shared.MyConstants;

/**
 * This overlay provides last information before the user starts the livestream
 * @author Tom
 *
 */

public class GoLiveOverlay extends Composite {

	private VerticalPanel overlay = new VerticalPanel();
	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel titlePanel = new HorizontalPanel();
	private VerticalPanel checkBoxPanel = new VerticalPanel();
	private HorizontalPanel instructionPanel = new HorizontalPanel();

	private Button cancelButton;
	private Button overlayGoLiveButton;
	private CheckBox readCheckBox;
	
	public GoLiveOverlay(final View mainView, final VideoBroadcastServiceClientImpl serviceImpl, final String selectedPiece) {
		initWidget(overlay);
		
		setStyleName("overlay");	

		mainPanel.setStyleName("overlayMainPanel");

		HTML titleLabel = new HTML("You are about to go live!");
		titleLabel.getElement().setId("goLiveOverlayTitleLabel");
		
		this.titlePanel.add(titleLabel);
		this.titlePanel.setStyleName("overlayTitlePanel");

		instructionPanel.getElement().setId("goLiveOverlayInstructionPanel");
		HTML instructions = new HTML("<strong> Please make sure you considered the following points before you go live: </br> </br> </strong> <em>"
				+ " - You have a maximum recording time of " + MyConstants.MAX_TIME_LIVE / 60000 + " minutes </br>"
				+ " - Check in Wirecast if your microphone is on by watching the level meter while you make sounds </br>"
				+ " - Check in Wirecast if your webcam captures what you want to show </br> "
//				+ " - Play the piece: <span class=\"selectedPiece\"> " + this.selectedPiece + "</span> "  //  </br> &nbsp 
						+ "</em>");
		instructionPanel.add(instructions);
		
		readCheckBox = new CheckBox("I rechecked the operability of my webcam and my microphone \n"
				+ " and confirm that I will only play: ");
		
		checkBoxPanel.getElement().setId("goLiveOverlayCheckBoxPanel");
		readCheckBox.setStyleName("goLiveOverlayCheckBox");
		checkBoxPanel.add(readCheckBox);
		checkBoxPanel.add(new HTML("</br> <p class=\"overlaySelectedPiece\" style=\"margin: auto;"
				+ "text-align: center; font-size: 1.2em;\"> " + selectedPiece + "</p> "));
		
//		final FocusPanel imagePanel = new FocusPanel();
//		imagePanel.setStyleName("overlayExplanationImagePanel");
//		Image explanationImage = new Image(Resources.INSTANCE.explanation().getSafeUri());
//		explanationImage.setPixelSize(16,16);
//		imagePanel.add(explanationImage);
//		checkBoxPanel.add(imagePanel);
		
		readCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (readCheckBox.getValue()) {
					overlayGoLiveButton.setEnabled(true);
					overlayGoLiveButton.setStyleName("overlayButton");
				} else {
					overlayGoLiveButton.setEnabled(false);
					overlayGoLiveButton.setStyleName("overlayButtonDisabled");
				}
			}
		});
		
		cancelButton = new Button("Cancel");
		overlayGoLiveButton = new Button("Ok");
		cancelButton.setStyleName("overlayButton");
		overlayGoLiveButton.setStyleName("overlayButton");
		
		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.getElement().setId("overlayButtonPanel");
		buttonPanel.add(cancelButton);
		buttonPanel.add(overlayGoLiveButton);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mainView.enableGoLiveButton(true);
				mainView.showGoLiveOverlay(false);
				mainView.showSelectedPieceMessage(selectedPiece);
				mainView.setLiveStatusLabel("offline");
			}		
		});
		
		overlayGoLiveButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mainView.isAuthenticated = false;
				mainView.setGoLiveOverlayButtonsEnabled(false);
				
				serviceImpl.makeBroadcastLive();
				
				Timer t = new Timer() {
					
					@Override
					public void run() {
						if (!mainView.isAuthenticated && readCheckBox.getValue())
							mainView.setGoLiveOverlayButtonsEnabled(true);
					}
				};
				t.schedule(3000);	
			}
		});
		
		
		overlayGoLiveButton.setEnabled(false);
		overlayGoLiveButton.setStyleName("overlayButtonDisabled");
		
		mainPanel.add(titlePanel);
		mainPanel.add(instructionPanel);
		mainPanel.add(checkBoxPanel);
		mainPanel.add(buttonPanel);
		
		overlay.add(this.mainPanel);
		cancelButton.setFocus(true);

	}

	public Button getCancelButton() {
		return cancelButton;
	}
	
	public Button getOverlayGoLiveButton() {
		return overlayGoLiveButton;
	}

	public void setOverlayGoLiveButtonFocused() {
		this.cancelButton.setFocus(true);
	}
	
}
