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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This overlay first prompts the user to type in/to choose an IMSLP piece and then
 * to insert an url to a Youtube video to be linked with IMSLP
 * @author Tom
 *
 */

public class LinkPieceOverlay extends Composite {

	private VerticalPanel overlay = new VerticalPanel();
	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel titlePanel = new HorizontalPanel();
	private HorizontalPanel suggestBoxPanel = new HorizontalPanel();
	private HorizontalPanel validationPanel = new HorizontalPanel();
	private HorizontalPanel validationTickPanel = new HorizontalPanel();
	private HorizontalPanel validationCrossPanel = new HorizontalPanel();
	private HorizontalPanel validationBusyPanel = new HorizontalPanel();
	private VerticalPanel checkBoxPanel = new VerticalPanel();
	private HorizontalPanel checkBoxPanel2 = new HorizontalPanel();
	private MySuggestBox suggestBox;
	Image cross = new Image("http://i.imgur.com/6YbOVVU.png");
	Image tick = new Image("http://i.imgur.com/wHRzKKU.png");
	Image wait = new Image("http://i.imgur.com/xXVcjNm.gif");
	
	Timer timer;
	private Button cancelButton;
	private Button okButton;
	private PopupPanel popupPanel;
	private PopupPanel popupPanel2;
	private CheckBox ackCheckBox = new CheckBox("I guarantee that my linked video contains the performance of ");
	private CheckBox onIMSLPCheckBox = new CheckBox("Show video on IMSLP.org");
	private VideoBroadcastServiceClientImpl serviceImpl;
	private MultiWordSuggestOracle oracle;
	private boolean isSuggestBoxFocused = false;
	private boolean isPieceAccepted;
	private String pieceToLink;
	private Label titleLabel;
	private HorizontalPanel buttonPanel;
	private boolean showLinkOptions = false;
	private boolean showSuccess = false;
	private SearchBox textBox;
	
	public LinkPieceOverlay(final View mainView, final VideoBroadcastServiceClientImpl serviceImpl) {
		initWidget(overlay);
		
		setStyleName("overlay");	

		this.serviceImpl = serviceImpl;
		
		mainPanel.setStyleName("overlayMainPanel");

		titleLabel = new Label("1. Type in or paste the piece you want to link with your video");
		titleLabel.getElement().setId("linkVideoOverlayTitleLabel");
		HorizontalPanel imslpLinkPanel = new HorizontalPanel();
		imslpLinkPanel.setStyleName("imslpLinkPanel");
		Anchor imslpLink = new Anchor("Petrucci Music Library");
		imslpLink.setTarget("_blank");
		imslpLink.setHref("http://imslp.org");
		imslpLink.setStyleName("imslpLink");
		imslpLinkPanel.add(imslpLink);
		
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
		this.titlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		this.titlePanel.setStyleName("linkVideoOverlayTitlePanel");
		
		timer = new Timer() {
			@Override
			public void run() {
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
		suggestBoxPanel.setStyleName("overlaySuggestBoxPanel");
		
		checkBoxPanel.setStyleName("overlayCheckBoxPanel");
		checkBoxPanel2.setStyleName("overlayCheckBoxPanel");
		checkBoxPanel2.getElement().setId("overlayOnIMSLPCheckBox");
		onIMSLPCheckBox.setStyleName("checkBox");
		checkBoxPanel2.add(onIMSLPCheckBox);
		checkBoxPanel.add(ackCheckBox);
		ackCheckBox.setStyleName("linkVideoOverlayAckCheckBox");
		ackCheckBox.setEnabled(false);
		onIMSLPCheckBox.setValue(true);
		onIMSLPCheckBox.setEnabled(false);
		
		ackCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (ackCheckBox.getValue()) {
					setLinkVideoButtonEnabled(true);
				} else {
					setLinkVideoButtonEnabled(false);
				}
			}
		});
		
		final FocusPanel imagePanel = new FocusPanel();
		imagePanel.setStyleName("overlayExplanationImagePanel");
		String explUrl = "http://i.imgur.com/0bZ4ie9.png";
		final Image explanationImage = new Image(explUrl);
		explanationImage.setPixelSize(22,22);
		imagePanel.add(explanationImage);
		popupPanel = new PopupPanel();
		
		explanationImage.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				popupPanel.hide();
			}
		});
		
		explanationImage.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				
				popupPanel = new PopupPanel();
				HorizontalPanel childPanel = new HorizontalPanel();
				
				childPanel.add(new HTML("<em> You have to type in the exact title of the Petrucci library piece you want to play. "
						+ "We recommend to first check how the piece is called on IMSLP and to copy and paste its title "
						+ "into the textbox. </em> "));
				childPanel.setStyleName("popupPanelChild");
				popupPanel.setStyleName("popupPanel");
				popupPanel.add(childPanel);
				popupPanel.setAnimationEnabled(true);
				
				popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						popupPanel.setPopupPosition(explanationImage.getAbsoluteLeft() + 25, explanationImage.getAbsoluteTop() - 10);
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
		
		this.titlePanel.add(titleLabel);
		titlePanel.add(explanationImage);
		this.titlePanel.add(imslpLinkPanel);
		
		cancelButton = new Button("Cancel");
		okButton = new Button("Ok");
		cancelButton.setStyleName("overlayButton");
		okButton.setStyleName("overlayButton");
		
		buttonPanel = new HorizontalPanel();
		buttonPanel.getElement().setId("overlayButtonPanel");
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mainView.showLinkPieceOverlay(false);
			}		
		});
		
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if (showSuccess) {
					mainView.showLinkPieceOverlay(false);
				} else if (showLinkOptions) {
							
					String videoId = "";
					String url = textBox.getText();
					
			        String[] urlParts = url.split("\\?");
					
			        for (String urlPart:urlParts) {
			        	if (urlPart.substring(0,2).equals("v=")) {
			        		videoId = urlPart.substring(2);
			        		break;
			        	} 
			        }
			        
			        if (!videoId.equals("")) {
			        	mainView.setCursorWaiting(true);
			        	setLinkVideoButtonEnabled(false);
			        	serviceImpl.checkIfVideoIsAvailable(videoId);
			        	GATracker.trackEvent("Link video", "Valid url");
			        } else {
			        	GATracker.trackEvent("Link video", "Invalid url");
			        	Window.alert("Invalid url. Please enter the whole url of your Youtube video!");
			        }
					
				} else {
					pieceToLink = suggestBox.getText();
					serviceImpl.doFinalPieceCheck(true, pieceToLink);
					/* The callback calls showFinalPieceCheckResult() */
				}
			}		
		});		

		mainPanel.add(titlePanel);
		mainPanel.add(suggestBoxPanel);
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
		oracle.clear();
		this.serviceImpl.getWikiPages(true, suggestBox.getText());
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
		serviceImpl.checkPiece(true, title);
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
	
			serviceImpl.setPieceToLink(pieceToLink);
			showLinkOptions();
			ackCheckBox.setEnabled(true);
			
			GATracker.trackEvent("Link video", "checkPiece", "Piece exists");
		}
		else {
			ackCheckBox.setEnabled(false);
			ackCheckBox.setValue(false);
			validationPanel.clear();
			validationPanel.add(validationCrossPanel);
			GATracker.trackEvent("Link video", "checkPiece", "Piece doesn't exist");
		}
	}

	private void showLinkOptions() {
		
		showLinkOptions = true;
		okButton.setEnabled(false);
		okButton.setStyleName("overlayButtonDisabled");
		
		mainPanel.clear();

		this.titlePanel.clear();
		titleLabel.setText("2. Now paste the url of your Youtube video");
		this.titlePanel.add(titleLabel);

		HorizontalPanel textBoxPanel = new HorizontalPanel();
		textBoxPanel.setStyleName("overlaySuggestBoxPanel");
		textBox = new SearchBox("Paste your url here", "linkVideoOverlayTextBoxDefault", "linkVideoOverlayTextBox");
		textBoxPanel.add(textBox);
		
		checkBoxPanel.add(ackCheckBox); 
		
		checkBoxPanel.add(new HTML("</br> <p class=\"overlaySelectedPiece\" style=\"margin: auto;"
				+ "text-align: center; font-size: 1.2em;\"> " + pieceToLink + "</p> "));
		
		
		mainPanel.add(titlePanel);
		mainPanel.add(textBoxPanel);
		mainPanel.add(checkBoxPanel);
		mainPanel.add(new HTML("</br> </br>"));
		mainPanel.add(buttonPanel);
		
	}

	public SuggestBox getSuggestBox() {
		return this.suggestBox;
	}

	public void showLinkPieceSuccess() {
		showSuccess = true;
		buttonPanel.remove(cancelButton);

		buttonPanel.getElement().setId("overlayButtonPanelCenter");
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		mainPanel.clear();

		this.titlePanel.clear();
		titleLabel.addStyleName("linkVideoOverlayTitleCenter");
		titleLabel.setText("Success! Your video has been linked with IMSLP.");
		this.titlePanel.add(titleLabel);
		
		String pieceToLinkEncoded = URL.encodeQueryString(pieceToLink).replace("+", "_");;
		String imslpLink = "//imslp.org/wiki/" + pieceToLinkEncoded;
		
		mainPanel.add(titlePanel);
		mainPanel.add(new HTML("<p style=\"width: 100%; text-align: center; padding-left: 90px; font-size: 1.2em;\"> "
				+ " Your video can be watched on <a href=\"" + imslpLink + "\" target=\"_blank_\"> IMSLP </a> and in our TV section!"
				+ " </br> </p>"));
		mainPanel.add(buttonPanel);
		
	}

	public void setLinkVideoButtonEnabled(boolean enabled) {
		if (enabled) {
			okButton.setEnabled(true);
			okButton.setStyleName("overlayButton");
		} else {
			okButton.setEnabled(false);
			okButton.setStyleName("overlayButtonDisabled");		
		}
	}

}
