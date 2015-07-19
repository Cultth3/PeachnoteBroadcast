package com.videobroadcast.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @deprecated
 * @author Tom
 *
 */

public class StartOverlay extends Composite {

	private VerticalPanel overlay = new VerticalPanel();
	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel titlePanel = new HorizontalPanel();

	Timer timer;
	private Button cancelButton;
	private Button okButton;
	private PopupPanel popupPanel;
	private PopupPanel popupPanel2;
	
	public StartOverlay(final View mainView) {
		initWidget(overlay);
		
		setStyleName("overlay");	

		mainPanel.setStyleName("overlayMainPanel");

		Label titleLabel = new HTML("<div style=\"text-align: center; width: 760px;\"> Welcome to Music Live Broadcasting! </div> ");
		titleLabel.setStyleName("startOverlayTitleLabel");
		
		this.titlePanel.add(titleLabel);
		cancelButton = new Button("Cancel");
		okButton = new Button("Ok");
		cancelButton.setStyleName("overlayButton");
		okButton.setStyleName("overlayButton");
		
		VerticalPanel subTitlePanel = new VerticalPanel();
		HTML subTitleHTML = new HTML("<p style=\"width: 100%; height: 40px; text-align: center;"
				+ "font-weight: bold; font-size: 1.3em;  \"> Here you can watch people filming their music interpretations live - or record or own!</p>");
		subTitlePanel.add(subTitleHTML);
		
//		final Image eye = new Image(Resources.INSTANCE.eye().getSafeUri());
//		final Image eyeMuted = new Image(Resources.INSTANCE.eyeMuted().getSafeUri());
		final Image eye = new Image("http://i.imgur.com/1vtDMeh.png");
		final Image eyeMuted = new Image("http://i.imgur.com/PIxAmvl.png");
		eye.setPixelSize(128, 128);
		eyeMuted.setPixelSize(128, 128);
		
//		final Image shutter = new Image(Resources.INSTANCE.shutter().getSafeUri());
//		final Image shutterMuted = new Image(Resources.INSTANCE.shutterMuted().getSafeUri());
		final Image shutter = new Image("http://i.imgur.com/2IGYPfS.png");
		final Image shutterMuted = new Image("http://i.imgur.com/1PciTQl.png");
		shutter.setPixelSize(86, 86);
		shutterMuted.setPixelSize(86, 86);
		
		HorizontalPanel imagePanel = new HorizontalPanel();
		imagePanel.getElement().setId("startOverlayImagePanel");
		imagePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		FocusPanel watchPanelWrapper = new FocusPanel();
		VerticalPanel watchPanelInnerWrapper = new VerticalPanel();
		watchPanelInnerWrapper.setStyleName("startOverlayInnerWrapper");
		final VerticalPanel watchPanel = new VerticalPanel();
		watchPanel.setStyleName("startOverlayImagesPanel");
		watchPanelWrapper.setStyleName("startOverlayImageWrapper");
		watchPanelInnerWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		watchPanelInnerWrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		watchPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		watchPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		watchPanel.add(eyeMuted);

		watchPanelInnerWrapper.add(new HTML("<p style=\"font-weight: bold; font-size: 1.3em; text-align: center; margin: auto;\"> Watch interpretations! </p>"));
		watchPanelInnerWrapper.add(watchPanel);
		watchPanelWrapper.add(watchPanelInnerWrapper);
		
		FocusPanel recordPanelWrapper = new FocusPanel();
		VerticalPanel recordPanelInnerWrapper = new VerticalPanel();
		final VerticalPanel recordPanel = new VerticalPanel();
		recordPanel.setStyleName("startOverlayImagesPanel");
		recordPanelInnerWrapper.setStyleName("startOverlayInnerWrapper");
		recordPanelWrapper.setStyleName("startOverlayImageWrapper");
		recordPanelInnerWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		recordPanelInnerWrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		recordPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		recordPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		recordPanel.add(shutterMuted);
		
		
		
		recordPanelInnerWrapper.add(new HTML("<p style=\"font-weight: bold; font-size: 1.3em; text-align: center; margin: auto;\"> Go live and play! </p>"));
		recordPanelInnerWrapper.add(recordPanel);
		recordPanelWrapper.add(recordPanelInnerWrapper);
		
		imagePanel.add(recordPanelWrapper);
		imagePanel.add(watchPanelWrapper);
		
		watchPanelWrapper.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				watchPanel.clear();
				watchPanel.add(eye);
			}
		});
		
		watchPanelWrapper.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				watchPanel.clear();
				watchPanel.add(eyeMuted);
			}
		});
		
		recordPanelWrapper.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				recordPanel.clear();
				recordPanel.add(shutter);
			}
		});
		
		recordPanelWrapper.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				recordPanel.clear();
				recordPanel.add(shutterMuted);
			}
		});
		

		watchPanelWrapper.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mainView.showTVSection(false);
//				mainView.showStartOverlay(false);
				GATracker.trackEvent("Sections", "User chose TV section");
			}
		});
		
		recordPanelWrapper.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mainView.showGoLiveSection(false);				
//				mainView.showStartOverlay(false);
				GATracker.trackEvent("Sections", "User chose Go Live section");
			}
		});
		
		
		imagePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		subTitlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		mainPanel.add(titlePanel);
		mainPanel.add(subTitleHTML);
		mainPanel.add(imagePanel);
		
		overlay.add(this.mainPanel);

	}
	
	public void hidePopupPanels() {
		popupPanel.hide();
		popupPanel2.hide();		
	}

	public void setCancelButtonFocused() {
		this.cancelButton.setFocus(true);
	}

}
