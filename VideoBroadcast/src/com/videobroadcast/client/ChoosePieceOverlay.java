package com.videobroadcast.client;

import java.util.List;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This overlay prompts the user to type in/to choose an IMSLP piece
 * and the quality/bit rate for the stream + broadcast 
 * @author Tom
 *
 */

public class ChoosePieceOverlay extends Composite {

	private VerticalPanel overlay = new VerticalPanel();
	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel titlePanel = new HorizontalPanel();
	private HorizontalPanel suggestBoxPanel = new HorizontalPanel();
	private HorizontalPanel validationPanel = new HorizontalPanel();
	private HorizontalPanel validationTickPanel = new HorizontalPanel();
	private HorizontalPanel validationCrossPanel = new HorizontalPanel();
	private HorizontalPanel validationBusyPanel = new HorizontalPanel();
	private HorizontalPanel checkBoxPanel = new HorizontalPanel();
	private HorizontalPanel checkBoxPanel2 = new HorizontalPanel();
	private MySuggestBox suggestBox;

	Image cross = new Image("http://i.imgur.com/6YbOVVU.png");
	Image tick = new Image("http://i.imgur.com/wHRzKKU.png");
	Image wait = new Image("http://i.imgur.com/xXVcjNm.gif");
	
	final private View mainView;

	Timer timer;
	private Button cancelButton;
	private Button okButton;
	private PopupPanel popupPanel;
	private PopupPanel popupPanel2;
	private CheckBox isPublicCheckBox = new CheckBox("Show video on Youtube");
	private CheckBox onIMSLPCheckBox = new CheckBox("Show video on IMSLP.org");
	private VideoBroadcastServiceClientImpl serviceImpl;
	private MultiWordSuggestOracle oracle;
	private boolean isSuggestBoxFocused = false;
	private boolean isPieceAccepted;
	private String selectedPiece;
	private String format;
	private VerticalPanel radioButtonPanel = new VerticalPanel();
	private RadioButton lowestRadioButton;
	private RadioButton lowRadioButton;
	private RadioButton midRadioButton;
	private RadioButton highRadioButton;
	private RadioButton veryHighRadioButton;
	
	public ChoosePieceOverlay(final View mainView, final VideoBroadcastServiceClientImpl serviceImpl, String format) {
		initWidget(overlay);
		
		setStyleName("overlay");	

		this.mainView = mainView;
		this.serviceImpl = serviceImpl;
		this.format = format;
		
		mainPanel.setStyleName("overlayMainPanel");

		Label titleLabel = new Label("Choose a piece of the Petrucci Music Library you want to play");
		titleLabel.getElement().setId("overlayTitleLabel");
		Anchor imslpLink = new Anchor("Petrucci Music Library");
		imslpLink.setTarget("_blank");
		imslpLink.setHref("http://imslp.org");
		imslpLink.setStyleName("imslpLink");
		
		HorizontalPanel validationTickIconPanel = new HorizontalPanel();
		validationTickIconPanel.add(tick);
		Label validationTickLabel = new Label("Good choice!");
		validationTickPanel.add(validationTickIconPanel);
		validationTickPanel.add(validationTickLabel);
		validationTickPanel.setStyleName("validationPanel");
		validationTickIconPanel.setStyleName("validationIconPanel");
		validationTickLabel.addStyleName("validationGreen");
		
		HorizontalPanel validationCrossIconPanel = new HorizontalPanel();
		validationCrossIconPanel.add(cross);
		HTML validationCrossLabel = new HTML("This piece doesn't exist in </br> the Petrucci Music Library!");
		validationCrossPanel.add(validationCrossIconPanel);
		validationCrossPanel.add(validationCrossLabel);
		validationCrossPanel.setStyleName("validationPanel");
		validationCrossIconPanel.setStyleName("validationIconPanel");
		validationCrossLabel.addStyleName("validationRed");
		
		HorizontalPanel validationBusyIconPanel = new HorizontalPanel();
		validationBusyIconPanel.add(wait);
		Label validationBusyLabel = new Label("searching ...");
		validationBusyPanel.add(validationBusyIconPanel);
		validationBusyPanel.add(validationBusyLabel);
		validationBusyPanel.setStyleName("validationPanel");
		validationBusyIconPanel.setStyleName("validationIconPanel");
		validationBusyLabel.addStyleName("validationGrey");
		
		this.titlePanel.add(titleLabel);
		this.titlePanel.add(imslpLink);
		
		timer = new Timer() {
			@Override
			public void run() {
//				setPieceAccepted(false);
				updateSuggestions();
			}
		};
		
		oracle = new MultiWordSuggestOracle();
		suggestBox = new MySuggestBox(oracle);
//		suggestBox.setLimit(15);
		
		suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				checkPiece(suggestBox.getText());
			}
		});
		
		suggestBox.getValueBox().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (isSuggestBoxFocused && !isPieceAccepted && suggestBox.getText().length() >= 1) 
					updateSuggestions();				
			}
		});
		
		suggestBox.getValueBox().addFocusHandler(new FocusHandler() {
			
			@Override
			public void onFocus(FocusEvent event) {
				isSuggestBoxFocused = true;
			}
		});
		
		suggestBox.getValueBox().addBlurHandler(new BlurHandler() {
			
			@Override
			public void onBlur(BlurEvent event) {
				isSuggestBoxFocused = false;
			}
		});		
		
		KeyUpHandler suggestBoxHandler = new KeyUpHandler() {
			
			private boolean validCharacter;
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (suggestBox.getText().length() == 0) {
//					timer.cancel();
					validationPanel.clear();
				} else {
					
					if (suggestBox.getText().length() >= 1) {
					
						validCharacter = true;
						
						int code = event.getNativeKeyCode();
						switch (code) {
			            case KeyCodes.KEY_LEFT:
			            case KeyCodes.KEY_RIGHT:
		//	            case KeyCodes.KEY_BACKSPACE:
			            case KeyCodes.KEY_TAB:
			            case KeyCodes.KEY_ALT:
			            case KeyCodes.KEY_SCROLL_LOCK:
			            case KeyCodes.KEY_SHIFT:
			            case KeyCodes.KEY_CAPS_LOCK:
			            case KeyCodes.KEY_CONTEXT_MENU:
			            case KeyCodes.KEY_DOWN:
			            case KeyCodes.KEY_UP:
			            case KeyCodes.KEY_CTRL:
			            case KeyCodes.KEY_END:
			            case KeyCodes.KEY_ESCAPE:
			            case KeyCodes.KEY_F1:
			            case KeyCodes.KEY_F2:
			            case KeyCodes.KEY_F3:
			            case KeyCodes.KEY_F4:
			            case KeyCodes.KEY_F5:
			            case KeyCodes.KEY_F6:
			            case KeyCodes.KEY_F7:
			            case KeyCodes.KEY_F8:
			            case KeyCodes.KEY_F9:
			            case KeyCodes.KEY_F10:
			            case KeyCodes.KEY_F11:
			            case KeyCodes.KEY_F12:
			            case KeyCodes.KEY_FIRST_MEDIA_KEY:
			            case KeyCodes.KEY_HOME:
			            case KeyCodes.KEY_INSERT:
			            case KeyCodes.KEY_NUM_CENTER:
			            case KeyCodes.KEY_NUM_DIVISION:
			            case KeyCodes.KEY_NUMLOCK:
			            case KeyCodes.KEY_PAGEDOWN:
			            case KeyCodes.KEY_PAGEUP:
			            case KeyCodes.KEY_PAUSE:
			            case KeyCodes.KEY_PRINT_SCREEN:
			            case KeyCodes.KEY_WIN_KEY_RIGHT:
			            case KeyCodes.KEY_WIN_KEY_FF_LINUX:
			            case KeyCodes.KEY_WIN_IME:
			            case KeyCodes.KEY_WIN_KEY:
			            case KeyCodes.KEY_WIN_KEY_LEFT_META:
							// Do nothing
			            validCharacter = false;
						}
		
						if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
							checkPiece(suggestBox.getText());
						} else if (validCharacter) {
							validationPanel.clear();
							validationPanel.add(validationBusyPanel);
							timer.cancel();
							timer.schedule(1000);
						}
					}
				}
			}
		};
		
		suggestBox.addKeyUpHandler(suggestBoxHandler);
		suggestBox.setAutoSelectEnabled(false);
		
		suggestBoxPanel.add(suggestBox);
		suggestBoxPanel.add(validationPanel);
		suggestBoxPanel.getElement().setId("overlaySuggestBoxPanel");
		
		checkBoxPanel.setStyleName("overlayCheckBoxPanel");
		checkBoxPanel2.setStyleName("overlayCheckBoxPanel");
		checkBoxPanel2.getElement().setId("overlayOnIMSLPCheckBox");
		onIMSLPCheckBox.setStyleName("checkBox");
		isPublicCheckBox.setStyleName("checkBox");
		checkBoxPanel2.add(onIMSLPCheckBox);
		checkBoxPanel.add(isPublicCheckBox);
		onIMSLPCheckBox.setValue(true);
		onIMSLPCheckBox.setEnabled(false);
		
		final FocusPanel imagePanel = new FocusPanel();
		imagePanel.setStyleName("overlayExplanationImagePanel");
		String explUrl = "http://i.imgur.com/0bZ4ie9.png";
		Image explanationImage = new Image(explUrl);
		explanationImage.setPixelSize(16,16);
		imagePanel.add(explanationImage);
		checkBoxPanel.add(imagePanel);
		popupPanel = new PopupPanel();
		
		explanationImage.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				popupPanel.hide(); //
			}
		});
		
		explanationImage.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				
				popupPanel = new PopupPanel();
				HorizontalPanel childPanel = new HorizontalPanel();
				
				childPanel.add(new HTML("<em> If this option is enabled, your video will be public. "
						+ "Public performances can not only be watched on IMSLP but also on Youtube. "
						+ "They also can be found over the Youtube search. "
						+ "</br> If it is disabled your performance can only be seen on the "
						+ "IMSLP page of your chosen piece and in the TV section of this website. </em>  "));
				childPanel.setStyleName("popupPanelChild");
				popupPanel.setStyleName("popupPanel");
				popupPanel.add(childPanel);
				popupPanel.setAnimationEnabled(true);
				
				popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						popupPanel.setPopupPosition(imagePanel.getAbsoluteLeft() + 25, imagePanel.getAbsoluteTop() - 10);
					}
				});
			}
		});
		
		final FocusPanel imagePanel2 = new FocusPanel();
		imagePanel2.setStyleName("overlayExplanationImagePanel");
		Image explanationImage2 = new Image(explUrl);
		explanationImage2.setPixelSize(16,16);
		imagePanel2.add(explanationImage2);
		checkBoxPanel2.add(imagePanel2);
		popupPanel2 = new PopupPanel();
		
		explanationImage2.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				popupPanel2.hide();
			}
		});
		
		explanationImage2.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				
				popupPanel2 = new PopupPanel();
				HorizontalPanel childPanel = new HorizontalPanel();
				
				childPanel.add(new HTML("<em> Your video can be watched on the Petrucci Music Library page of your chosen piece"
						+ " and in the TV section of this web page. </em> "));
				childPanel.setStyleName("popupPanelChild");
				popupPanel2.setStyleName("popupPanel");
				popupPanel2.add(childPanel);
				popupPanel2.setAnimationEnabled(true);
				
				popupPanel2.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						popupPanel2.setPopupPosition(imagePanel2.getAbsoluteLeft() + 25, imagePanel2.getAbsoluteTop() - 10);
					}
				});
			}
		});
		
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
				mainView.showChoosePieceOverlay(false);
			}		
		});
		
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				selectedPiece = suggestBox.getText();
				serviceImpl.doFinalPieceCheck(false, selectedPiece);
				/* The callback calls showFinalPieceCheckResult() */ 
			}		
		});		
		
		HTML bitrateHTML = new HTML("You can test your internet connection here: <a href=\"http://www.speedtest.net/\" target=\"_blank\"> Speedtest </a> </br>"
				+ " Then select the maximum sustained bit rate that you can support and click ok!"); // <span style=\"font-weight: bold;\"> 
		lowestRadioButton = new RadioButton("radioButtons"," 300 Kbps -  700 Kbps  (240p)");
		lowRadioButton = new RadioButton("radioButtons"," 400 Kbps - 1000 Kbps  (360p)");
		midRadioButton = new RadioButton("radioButtons"," 500 Kbps - 2000 Kbps  (480p)");
		highRadioButton = new RadioButton("radioButtons","1500 Kbps - 4000 Kbps  (720p)");
		veryHighRadioButton = new RadioButton("radioButtons","3000 Kbps - 6000 Kbps (1080p)");
		HTML spaceHTML = new HTML("<div style=\"height: 6px; \"> </div>");
		HTML spaceHTML2 = new HTML("<div style=\"height: 12px; \"> </div>");
		
		 
		radioButtonPanel.add(bitrateHTML);
		radioButtonPanel.add(spaceHTML);
		radioButtonPanel.add(lowestRadioButton);
		radioButtonPanel.add(lowRadioButton);
		radioButtonPanel.add(midRadioButton);
		radioButtonPanel.add(highRadioButton);
		radioButtonPanel.add(veryHighRadioButton);
		radioButtonPanel.add(spaceHTML2);
		
		if (format == null || format.equals("360p"))
			lowRadioButton.setValue(true);
		else if (format.equals("240p")) {
			lowestRadioButton.setValue(true);
		} else if (format.equals("480p")) {
			midRadioButton.setValue(true);
		} else if (format.equals("720p")) {
			highRadioButton.setValue(true);
		} else if (format.equals("1080p")) {
			veryHighRadioButton.setValue(true);
		}
		
		lowRadioButton.setValue(true);
		radioButtonPanel.getElement().setId("formatRadioButtonPanel");
		
		DisclosurePanel disclosurePanel = new DisclosurePanel("Advanced settings");
		disclosurePanel.setStyleName("disclosurePanel");
		disclosurePanel.setAnimationEnabled(true);
		disclosurePanel.add(radioButtonPanel);
		
		mainPanel.add(titlePanel);
		mainPanel.add(suggestBoxPanel);
		mainPanel.add(checkBoxPanel2);
		mainPanel.add(checkBoxPanel);
		mainPanel.add(disclosurePanel);
		mainPanel.add(buttonPanel);
		
		overlay.add(this.mainPanel);

	}

	public void setSuggestBoxFocused() {
		this.suggestBox.setFocus(true);
	}
	
	public Button getCancelButton() {
		return cancelButton;
	}
	
	public Button getCreateLiveStreamButton() {
		return okButton;
	}
	
	public void showPieceAcceptedResult(boolean accepted) {
		isPieceAccepted = accepted;
		if (accepted) {
			validationPanel.clear();
			validationPanel.add(validationTickPanel);
		} else {
			validationPanel.clear();
			validationPanel.add(validationCrossPanel);
		}
		suggestBox.setEnabled(true);
	}

	private void updateSuggestions() {
//		timer.cancel();
		oracle.clear();
		this.serviceImpl.getWikiPages(false, suggestBox.getText());
//		timer.schedule(1000);
	}
	
	/*
	 *  Called in the verifyInput() callback
	 */
	public void setWikiPages(List<String> result) {
		oracle.addAll(result);
		validationPanel.clear();
		suggestBox.showSuggestionList();
	}
	
	/* Check if piece exists */	
	private void checkPiece(String title) {
		validationPanel.clear();
		validationPanel.add(validationBusyPanel);
		suggestBox.setEnabled(false);
		serviceImpl.checkPiece(false, title);
	}
	

	class MySuggestBox extends SuggestBox {
        public MySuggestBox(MultiWordSuggestOracle oracle) {
            super(oracle);
            sinkEvents(Event.ONPASTE);
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            switch (event.getTypeInt()) {
            case Event.ONPASTE:
            	updateSuggestions();
                break;
            }
        }
    }


	public void hidePopupPanels() {
		popupPanel.hide();
		popupPanel2.hide();		
	}

	public void showFinalPieceCheckResult(boolean accepted) {
		if (accepted) {
	
			String privacyStatus;
			if (isPublicCheckBox.getValue()) {
				privacyStatus = "public";
			} else {
				privacyStatus = "unlisted";
			}
			serviceImpl.setSelectedPiece(selectedPiece);
			serviceImpl.setPrivacyStatus(privacyStatus);
			
			if (lowestRadioButton.getValue()) 
				format = "240p";
			else if (lowRadioButton.getValue())
				format = "360p";
			else if (midRadioButton.getValue())
				format = "480p";
			else if (highRadioButton.getValue())
				format = "720p";
			else 
				format = "1080p";
			serviceImpl.setFormat(format);
			mainView.showSelectedPieceMessage(selectedPiece);
			mainView.setIsBroadcastCreated(false);
			mainView.showChoosePieceOverlay(false);
			mainView.enableGoLiveButton(true);
			mainView.setProgressBarAfterChosenPiece();
			GATracker.trackEvent("Live streaming flow", "checkPiece", "Piece exists");
		}
		else {
			validationPanel.clear();
			validationPanel.add(validationCrossPanel);
			GATracker.trackEvent("Live streaming flow", "checkPiece", "Piece doesn't exist");
		}
	}

	public SuggestBox getSuggestBox() {
		return this.suggestBox;
	}

	public FocusWidget getIsPublicCheckBox() {
		return isPublicCheckBox;
	}

}
