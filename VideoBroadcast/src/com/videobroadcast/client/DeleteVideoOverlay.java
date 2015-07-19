package com.videobroadcast.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DeleteVideoOverlay extends Composite {

	private VerticalPanel overlay = new VerticalPanel();
	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel titlePanel = new HorizontalPanel();

	Timer timer;
	private Button cancelButton;
	private Button okButton;
	private PopupPanel popupPanel;
	private PopupPanel popupPanel2;
	
	public DeleteVideoOverlay(final View mainView, final TVListServiceClientImpl tvServiceImpl, String selectedPiece, final String broadcastId) {
		initWidget(overlay);
		
		setStyleName("overlay");	

		mainPanel.setStyleName("overlayMainPanel");

		Label titleLabel = new HTML("<div style=\"text-align: center; width: 760px;\"> Are you sure you want to delete: </p> <p class=\"overlaySelectedPiece\" style=\"text-align: center; width: 760px;\"> " + selectedPiece + "</span> <span style=\"color: black;\"> ? </span> </p>");
		titleLabel.setStyleName("overlayTitleLabel");
		
		this.titlePanel.add(titleLabel);
		cancelButton = new Button("Cancel");
		okButton = new Button("Ok");
		cancelButton.setStyleName("overlayButton");
		okButton.setStyleName("overlayButton");
		
		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.getElement().setId("overlayButtonPanel");
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mainView.hideDeleteVideoOverlay();
			}		
		});
		
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mainView.isAuthenticated = false;
				okButton.setEnabled(false);
				
				tvServiceImpl.deleteVideo(broadcastId);
				
				Timer t = new Timer() {
					@Override
					public void run() {
						if (!mainView.isAuthenticated)
							okButton.setEnabled(true);
					}
				};
				t.schedule(4000);
			}
			
		});
		
		mainPanel.add(titlePanel);
		mainPanel.add(buttonPanel);
		
		overlay.add(this.mainPanel);

	}
	
	public Button getCreateLiveStreamButton() {
		return okButton;
	}
	

	public void hidePopupPanels() {
		popupPanel.hide();
		popupPanel2.hide();		
	}

	public void setCancelButtonFocused() {
		this.cancelButton.setFocus(true);
	}

}
