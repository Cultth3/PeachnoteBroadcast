package com.videobroadcast.client;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.videobroadcast.shared.IdNameTuple;
import open.pandurang.gwt.youtube.client.ApiReadyEvent;
import open.pandurang.gwt.youtube.client.ApiReadyEventHandler;
import open.pandurang.gwt.youtube.client.PlayerConfiguration;
import open.pandurang.gwt.youtube.client.PlayerReadyEvent;
import open.pandurang.gwt.youtube.client.PlayerReadyEventHandler;
import open.pandurang.gwt.youtube.client.PlayerVariables;
import open.pandurang.gwt.youtube.client.YouTubePlayer;

public class View extends Composite {

	private static final String PLAYER_HEIGHT = "360";
	private static final String PLAYER_WIDTH = "640";
	
	private YouTubePlayer tvPlayer;
	private PlayerConfiguration config;
	private PlayerVariables playerVars;
	private VerticalPanel vPanel = new VerticalPanel();
	private VerticalPanel wrapperPanel;
	private VerticalPanel innerContentPanel;
	private VerticalPanel titlePanel;
	private HorizontalPanel footer;
	private HorizontalPanel buttonPanel;
	private VerticalPanel messagePanel;
	private VerticalPanel embeddedPlayerPanelWrapper;
	private VerticalPanel embeddedPlayerPanel;
	private Panel galleryPanelWrapper;
	private VerticalPanel liveStreamListPanel;
	private VerticalPanel listPanel;
	private Button changeVideoButton;
	private Button goLiveButton;
	private Button myUploadsButton;
	private Button choosePieceButton;
	private Button refreshPlayerButton;
	private Button refreshBroadcastListButton;
	private TextBox textBoxVideoId;
	private TextBox textBoxCommands;
	private Label messageLabel;
	private Anchor messageAnchor;
	private Panel galleryPanel;
	private HorizontalPanel controlPanel;
	private HorizontalPanel progressBarPanel;
	private HorizontalPanel progressBarPanelWrapper;
	private HorizontalPanel liveStatusPanel;
	private Label liveStatusLabel;
	private HTML liveStatusValue;
	
	private ExplanationGallery gallery;
	private ProgressBarElement progressBarElement0;
	private ProgressBarElement progressBarElement1;
	private ProgressBarElement progressBarElement2;
	private ProgressBarElement progressBarElement3;
	
	VideoBroadcastServiceClientImpl serviceImpl;
	private boolean keepStepTwoDone;
	private boolean catchPause = false;
	private HorizontalPanel footerVideoPanel;
	private String broadcastId;
	private Timer checkPlayerStateTimer;
	private HorizontalPanel footerVideoGlassPanel;
	private ChoosePieceOverlay overlay;
	private GoLiveOverlay goLiveOverlay;
	private StopStreamOverlay stopStreamOverlay;
	private HorizontalPanel overlayBack; 
	private boolean isBroadcastCreated = false;
	protected boolean isAuthenticated = false;
	public boolean choosePieceButtonClicked = false;
	private String firstTVSectionBroadcastId;
	private TVListServiceClientImpl tvServiceImpl;
	private DeleteVideoOverlay deleteVideoOverlay;
	private Widget titleLabel;
	private Widget goLiveSubtitleLabel;
	private Label tvSectionSubtitleLabel;
	private Label linkVideoSubtitleLabel;
	private Label aboutSubtitleLabel;
	private FocusPanel tvSectionFooterEntry;
	private FocusPanel goLiveSectionFooterEntry;
	private FocusPanel linkVideosFooterEntry;
	private FocusPanel aboutFooterEntry;
	private boolean trackTVsection = false;
	private boolean trackLinkVideoSection = false;
	private LinkPieceOverlay linkPieceOverlay;
	private VerticalPanel fixedFooter;
	private Button button;
	
	public View(final VideoBroadcastServiceClientImpl serviceImpl, TVListServiceClientImpl tvServiceImpl) {
		initWidget(this.vPanel);
		this.serviceImpl = serviceImpl;
		this.tvServiceImpl = tvServiceImpl;
		
		
//		RootPanel.get().add(this.vPanel);
		this.wrapperPanel = new VerticalPanel();
		this.innerContentPanel = new VerticalPanel();
		this.footer = new HorizontalPanel();
		this.wrapperPanel.add(footer); // TODO: footer
		this.wrapperPanel.add(innerContentPanel);
		this.vPanel.add(wrapperPanel);
		
		this.titlePanel = new VerticalPanel();
		this.galleryPanelWrapper = new VerticalPanel();
		this.buttonPanel = new HorizontalPanel();
		this.messagePanel = new VerticalPanel();
		this.galleryPanel = new VerticalPanel();
		this.controlPanel = new HorizontalPanel();
		this.progressBarPanel = new HorizontalPanel();
		this.progressBarPanelWrapper = new HorizontalPanel();
		this.embeddedPlayerPanel = new VerticalPanel();
		this.embeddedPlayerPanelWrapper = new VerticalPanel();
		
		
		this.goLiveButton = new Button("Go live!");
		this.goLiveButton.setEnabled(false);
//		this.choosePieceButton = new Button(new HTML(new SafeHtmlBuilder().appendEscapedLines("Choose piece").toSafeHtml()).toString());
		this.choosePieceButton = new Button(new HTML("Choose piece").toString());
		
		titleLabel = new Label("Music Live Broadcasting");
		goLiveSubtitleLabel = new Label("4 steps and you are live to show your music interpretation to the world!");
		tvSectionSubtitleLabel = new Label("Watch interpretations by others or yourself!");
		linkVideoSubtitleLabel = new Label("Get more views by linking your Youtube videos with IMSLP!");
		aboutSubtitleLabel = new Label("About Music Live Broadcasting");
		messageLabel = new Label("");
		messageAnchor = new Anchor();
		
		/* Progress bar + gallery */
//		progressBarElement0 = new ProgressBarElement(this, "Step 1", 
//				new Image(Resources.INSTANCE.progressBarChoosePieceMuted().getSafeUri()), 
//				new Image(Resources.INSTANCE.progressBarChoosePiece().getSafeUri()));
//		progressBarElement1 = new ProgressBarElement(this, "Step 2", 
//				new Image(Resources.INSTANCE.progressBarWirecastMuted().getSafeUri()), 
//				new Image(Resources.INSTANCE.progressBarWirecast().getSafeUri()));
//		progressBarElement2 = new ProgressBarElement(this, "Step 3", 
//				new Image(Resources.INSTANCE.progressBarYoutubeMuted().getSafeUri()), 
//				new Image(Resources.INSTANCE.progressBarYoutube().getSafeUri()));
//		progressBarElement3 = new ProgressBarElement(this, "Step 4", 
//				new Image(Resources.INSTANCE.progressBarLiveMuted().getSafeUri()), 
//				new Image(Resources.INSTANCE.progressBarLive().getSafeUri()));
		
		progressBarElement0 = new ProgressBarElement(this, "Step 1", 
				new Image("http://i.imgur.com/hZYSWdT.png"), 
				new Image("http://i.imgur.com/AHfuCsV.png"));
		progressBarElement1 = new ProgressBarElement(this, "Step 2", 
				new Image("http://i.imgur.com/XWBLASS.png"), 
				new Image("http://i.imgur.com/s6jS15M.png"));
		progressBarElement2 = new ProgressBarElement(this, "Step 3", 
				new Image("http://i.imgur.com/eC0HslJ.png"), 
				new Image("http://i.imgur.com/ATZAFTa.png"));
		progressBarElement3 = new ProgressBarElement(this, "Step 4", 
				new Image("http://i.imgur.com/Ks3oXfY.png"), 
				new Image("http://i.imgur.com/H6qy4KQ.png"));
		
		progressBarElement0.setTitle("Choose a piece of the Petrucci Music Library you want to play");
		progressBarElement1.setTitle("Download and install Wirecast to send the signal of your webcam to Youtube");
		progressBarElement2.setTitle("Create a Youtube account/login to Youtube and enable livestreaming in your account");
		progressBarElement3.setTitle("Activate stream in Wirecast and go live!");

		FocusPanel progressBarElement0Wrapper = new FocusPanel(progressBarElement0);
		FocusPanel progressBarElement1Wrapper = new FocusPanel(progressBarElement1);
		FocusPanel progressBarElement2Wrapper = new FocusPanel(progressBarElement2);
		FocusPanel progressBarElement3Wrapper = new FocusPanel(progressBarElement3);
		
		progressBarElement0Wrapper.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setGalleryElementIndex(0);
				GATracker.trackEvent("Progress Bar", "Click on progress bar", "Click progress bar to \"ChoosePiece\" element ");
			}
		});
		progressBarElement1Wrapper.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setGalleryElementIndex(1);
				GATracker.trackEvent("Progress Bar", "Click on progress bar", "Click progress bar to \"Wirecast\" element ");
			}
		});	
		progressBarElement2Wrapper.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				setGalleryElementIndex(2);
				GATracker.trackEvent("Progress Bar", "Click on progress bar", "Click progress bar to \"Enable livestream\" element ");
			}
		});
		progressBarElement3Wrapper.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				setGalleryElementIndex(3);
				GATracker.trackEvent("Progress Bar", "Click on progress bar", "Click progress bar to \"live\" element ");
			}
		});
		
		this.progressBarPanel.add(progressBarElement0Wrapper);
		this.progressBarPanel.add(progressBarElement1Wrapper);
		this.progressBarPanel.add(progressBarElement2Wrapper);
		this.progressBarPanel.add(progressBarElement3Wrapper);
		
		this.gallery = new ExplanationGallery(this); // Progress bar must exist before creating the gallery!
		
		this.setGalleryElementIndex(0);
		galleryPanel.add(this.gallery);
		
		/* Fixed footer for Livestreaming controls */
		fixedFooter = new VerticalPanel();
		HorizontalPanel fixedFooterTop = new HorizontalPanel();
		HorizontalPanel fixedFooterControls = new HorizontalPanel();
		HorizontalPanel fixedFooterBottom = new HorizontalPanel();
		
		this.liveStatusValue = new HTML("offline");
		this.liveStatusLabel = new Label("Live status: ");
		this.liveStatusPanel = new HorizontalPanel();
		this.liveStatusValue.getElement().setId("liveStatusValue");
		this.liveStatusLabel.getElement().setId("liveStatusLabel");
		this.liveStatusPanel.getElement().setId("liveStatusPanel");
		this.liveStatusPanel.add(this.liveStatusLabel);
		this.liveStatusPanel.add(this.liveStatusValue);

//		fixedFooterTop.add(cameraImage);
//		fixedFooterTop.add(this.liveStatusLabel);
		fixedFooterControls.add(this.liveStatusPanel);
		fixedFooterControls.add(buttonPanel);
		fixedFooterControls.add(messagePanel);
			
//		footerVideoPanel = new HorizontalPanel();
//		footerVideoGlassPanel = new HorizontalPanel(); // Important to capture user's clicks on the Youtube monitor player
//		fixedFooterVideoContainer.add(footerVideoPanel);
		
		fixedFooterTop.add(fixedFooterControls);
//		fixedFooterTop.add(fixedFooterVideoContainer);
		fixedFooter.add(fixedFooterTop);
		fixedFooter.add(fixedFooterBottom);
		fixedFooterBottom.add(new Label(" ."));
		
		RootPanel.get().add(fixedFooter);
//		RootPanel.get().add(footerVideoPanel);
//		RootPanel.get().add(footerVideoGlassPanel);
		
		/* Normal footer */
		tvSectionFooterEntry = new FocusPanel(new HTML("<span style=\"vertical-align: middle;\"> TV section </span>"));
		goLiveSectionFooterEntry = new FocusPanel(new HTML("<span style=\"vertical-align: middle;\"> Go live section </span>"));
		linkVideosFooterEntry = new FocusPanel(new HTML("<span style=\"vertical-align: middle;\"> Link video </span>"));
		aboutFooterEntry = new FocusPanel(new HTML("<span style=\"vertical-align: middle; max-width: 100px;\"> About </span>"));
		footer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		tvSectionFooterEntry.setStyleName("footerEntry");
		goLiveSectionFooterEntry.setStyleName("footerEntry");
		linkVideosFooterEntry.setStyleName("footerEntrySmall");
		aboutFooterEntry.setStyleName("footerEntrySmall");
		goLiveSectionFooterEntry.addStyleName("currentFooterEntry");
		
		HorizontalPanel aboutFooterEntryPanel = new HorizontalPanel();
//		aboutFooterEntryPanel.setStyleName("footerEntrySmallPanels");
		aboutFooterEntryPanel.add(aboutFooterEntry);
//		aboutFooterEntryPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		HorizontalPanel linkVideosFooterEntryPanel = new HorizontalPanel();
//		linkVideosFooterEntryPanel.setStyleName("footerEntrySmallPanels");
		linkVideosFooterEntryPanel.add(linkVideosFooterEntry);
//		linkVideosFooterEntryPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		this.footer.add(goLiveSectionFooterEntry);
		this.footer.add(tvSectionFooterEntry);
		this.footer.add(linkVideosFooterEntryPanel);
		this.footer.add(aboutFooterEntryPanel);
			
		disableTextSelection(tvSectionFooterEntry.getElement(), true);
		disableTextSelection(goLiveSectionFooterEntry.getElement(), true);
		disableTextSelection(linkVideosFooterEntry.getElement(), true);
		disableTextSelection(aboutFooterEntry.getElement(), true);
		tvSectionFooterEntry.addStyleName("unselectable");
		goLiveSectionFooterEntry.addStyleName("unselectable");
		linkVideosFooterEntry.addStyleName("unselectable");
		aboutFooterEntry.addStyleName("unselectable");
		
		this.progressBarPanelWrapper.add(progressBarPanel);
		this.controlPanel.add(progressBarPanelWrapper);
		this.titlePanel.add(titleLabel);		
		this.titlePanel.add(goLiveSubtitleLabel);	
		this.buttonPanel.add(choosePieceButton);
//		Image dottedArrow = new Image(Resources.INSTANCE.dottedArrow().getSafeUri());
		Image dottedArrow = new Image("http://i.imgur.com/tpwKK6i.png");
		dottedArrow.getElement().setId("dottedArrow");
		dottedArrow.setPixelSize(40, 40);
		this.buttonPanel.add(dottedArrow);
		this.buttonPanel.add(goLiveButton);
		this.galleryPanelWrapper.add(galleryPanel);
		
		/* Website sections */
		goLiveSectionFooterEntry.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RootPanel.get().add(fixedFooter);
				showGoLiveSection(true);
			}
		});
		
		tvSectionFooterEntry.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (!serviceImpl.isLive) {
					RootPanel.get().remove(fixedFooter);
				}
				showTVSection(true);
			}
		});
		
		linkVideosFooterEntry.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (!serviceImpl.isLive) {
					RootPanel.get().remove(fixedFooter);
				}
				showLinkVideoSection();
			}
		});

		aboutFooterEntry.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (!serviceImpl.isLive) {
					RootPanel.get().remove(fixedFooter);
				}
				Window.scrollTo (0 ,0);			
				titlePanel.clear();
				titlePanel.add(titleLabel);
//				titlePanel.add(aboutSubtitleLabel);
				innerContentPanel.clear();
				innerContentPanel.add(titlePanel);

				tvSectionFooterEntry.removeStyleName("currentFooterEntry");
				goLiveSectionFooterEntry.removeStyleName("currentFooterEntry");
				linkVideosFooterEntry.removeStyleName("currentFooterEntry");
				aboutFooterEntry.addStyleName("currentFooterEntrySmall");
				
				VerticalPanel vPanel = new VerticalPanel();
				vPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
				vPanel.setStyleName("aboutSectionContentWrapper");
				
				HTML infoHtml = new HTML("<div style=\"vertical-align: top;\">"
						+ "<span style=\"font-size: 1.2em; font-weight: bold;\"> About </span> </br>"
						+ "</br> <span style=\"font-size: 0.9em;\">  "
						+ "The Music Live Broadcasting platform helps musicians to reach a larger audience for their classical music performances. "
						+ "If you like to record yourself playing classical music pieces you can go live and show your interpretations to a large audience live on "
						+ "Youtube and IMSLP.org. "
						+ "IMSLP, the Petrucci Music Library, contains almost all scores of known classical pieces and you can create livestreams for all library pieces. "
						+ "If you already have classical music performance videos on Youtube you can link them with IMSLP, so that your performance videos will be "
						+ "available for a large audience on IMSLP. </br>	"
						+ "The Music Live Broadcasting platform is a joint effort of Vladimir Viro and Thomas Steinbrecher "
						+ "and was created within the scope of Thomas' bachelor thesis: </br> <em> Live video streaming extension "
						+ "for the largest online sheet music community IMSLP.org </em> </span> </br>"
						+ "</br> The platform is still in development but all functions are already fully operative."
						+ "</br></br> <em> Contact: peachnotebroadcast@gmail.com </em> "
						+ " </div>"
//						+ "</br> <div> <p style=\"font-weight: bold; font-size: 1.2em;\"> Sign up </p> "
//						+ "<p> Sign up to get information about the progress of this website! </p>"
						+ "<div> <p style=\"font-weight: bold; font-size: 1.2em;\"> Impressum </p>"
						+ "<table> "
						+ "<tr> <td style=\"font-weight: bold;\"> Betreiber </td>  <td> Peachnote GMBH </td> </tr>"
						+ "<tr> <td style=\"font-weight: bold;\"> Geschäftsführer </td>  <td> Vladimir Viro </td> </tr>"
						+ "<tr> <td style=\"font-weight: bold;\"> Registergericht </td>  <td> Amtsgericht München </td> </tr>"
						+ "<tr> <td style=\"font-weight: bold;\"> Registernummer </td>  <td> HRB 215528 </td> </tr>"
						+ "<tr> <td style=\"font-weight: bold;\"> Umstatzsteuer Id &nbsp&nbsp&nbsp&nbsp </td>  <td> DE 291732592 (Finanzamt München) </td> </tr>"
						+ "<tr> <td style=\"font-weight: bold;\"> Adresse </td>  <td> Thorwaldsenstr. 6 </td> </tr>"
						+ "<tr> <td style=\"font-weight: bold;\"> </td>  <td> D-80335 München </td> </tr>"
//						+ "<tr> <td style=\"font-weight: bold;\"> E-Mail </td>  <td> ... </td> </tr>"
						+ "</table </div>");
				
				
				infoHtml.setStyleName("infoHTML");
				
				vPanel.add(infoHtml);
				innerContentPanel.add(vPanel);
				innerContentPanel.add(new HTML("</br> </br>"));
				
				GATracker.trackEvent("Sections", "About");
				
				// TODO
				
			}
		});
		
		this.embeddedPlayerPanel.getElement().setId("embeddedPlayerPanel");
		this.embeddedPlayerPanelWrapper.getElement().setId("embeddedPlayerPanelWrapper");

		this.innerContentPanel.add(titlePanel);
		
		initializeYouTubePlayer();
		
		/* Load GoLive-, TV- or LinkVideo-section dependently on the parameter */
		try {
			String param = Window.Location.getParameter("section");
			param = URL.decodeQueryString(param);
			if (param.equals("tvsection")) {
				titlePanel.clear();
				titlePanel.add(titleLabel);
				titlePanel.add(tvSectionSubtitleLabel);
				innerContentPanel.add(titlePanel);
				
				goLiveSectionFooterEntry.removeStyleName("currentFooterEntry");
				linkVideosFooterEntry.removeStyleName("currentFooterEntry");
				aboutFooterEntry.removeStyleName("currentFooterEntry");
				tvSectionFooterEntry.addStyleName("currentFooterEntry");

				RootPanel.get().remove(fixedFooter);
				
				createTVSectionListGallery();
				trackTVsection = true;
			} else if (param.equals("linkVideo")) {
				titlePanel.clear();
				titlePanel.add(titleLabel);
				titlePanel.add(linkVideoSubtitleLabel);
				innerContentPanel.add(titlePanel);
				
				goLiveSectionFooterEntry.removeStyleName("currentFooterEntry");
				linkVideosFooterEntry.addStyleName("currentFooterEntry");
				aboutFooterEntry.removeStyleName("currentFooterEntry");
				tvSectionFooterEntry.removeStyleName("currentFooterEntry");
				
				trackLinkVideoSection = true;
				
				RootPanel.get().remove(fixedFooter);
				showLinkVideoSection();
			} else {
				this.innerContentPanel.add(controlPanel);
				this.innerContentPanel.add(galleryPanelWrapper);
			}
		} catch (Exception e) {
			// No parameter given
			this.innerContentPanel.add(controlPanel);
			this.innerContentPanel.add(galleryPanelWrapper);
		}		
		
		/* Styles */
		this.vPanel.setStyleName("RootPanel");
		
//		footerVideoGlassPanel.getElement().setId("footerVideoGlassPanel");
//		footerVideoPanel.getElement().setId("footerVideoPanel");
		this.messagePanel.getElement().setId("messagePanel");
		fixedFooter.setStyleName("fixedFooter");
		fixedFooterTop.setStyleName("fixedFooterTop");
		fixedFooterControls.setStyleName("fixedFooterControls");
//		fixedFooterVideoContainer.setStyleName("fixedFooterVideoContainer");
		fixedFooterBottom.setStyleName("fixedFooterBottom");
		buttonPanel.setStyleName("buttonPanel");
		goLiveButton.setStyleName("overlayButtonDisabled2");
		goLiveButton.getElement().setId("goLiveButton");
		choosePieceButton.getElement().setId("choosePieceButton");
		choosePieceButton.setStyleName("overlayButton2");
		footer.setStyleName("footer");
		titlePanel.setStyleName("titlePanel");
		progressBarPanel.setStyleName("ProgressBarPanel");
		progressBarPanelWrapper.setStyleName("ProgressBarPanelWrapper");
		innerContentPanel.setStyleName("innerContentPanel");
		titleLabel.addStyleName("title");
		goLiveSubtitleLabel.addStyleName("subTitle");
		tvSectionSubtitleLabel.addStyleName("subTitle");
		goLiveSubtitleLabel.addStyleName("subTitle");
		linkVideoSubtitleLabel.addStyleName("subTitle");
		aboutSubtitleLabel.addStyleName("subTitle");
		galleryPanelWrapper.setStyleName("galleryPanel");
//		this.listPanel.setSpacing(10);
//		this.liveStreamListPanel.addStyleName("liveStreamListPanel");
		this.innerContentPanel.addStyleName("innerContentPanel");
//		this.buttonPanel.setSpacing(5);
//		this.buttonPanel.addStyleName("buttonPanel");
//		this.labelPanel.addStyleName("labelPanel");
//		this.labelPanel.setSpacing(10);
		this.galleryPanel.addStyleName("galleryPanel");
		
		
		this.choosePieceButton.addClickHandler(new ChoosePieceButtonHandler());
		this.goLiveButton.addClickHandler(new GoLiveButtonHandler());

		/* Instantiate WikiPages and set piece given as url parameter as selectedPiece */
		try {
			String param = Window.Location.getParameter("title");
			param = URL.decodeQueryString(param);
			final String title = param.replaceAll("_", " ");
			serviceImpl.instantiateWikiPages(title);
			this.setGalleryElementIndex(1);
			if (trackTVsection) {
				GATracker.trackEvent("Sections", "Start Section: TV section");
//				GATracker.trackEvent("Given piece: ", title);
			} else {
				GATracker.trackEvent("Sections", "Start Section: Go live section");
//				GATracker.trackEvent("Given piece: ", title);
			}
		} catch (Exception e) {
			// No parameter given
			serviceImpl.instantiateWikiPages(null);
			this.setGalleryElementIndex(0);
//			progressBarElement0.setDone(true);
			
			if (trackTVsection)
				GATracker.trackEvent("Sections", "Start Section: TV section");
			else if (trackLinkVideoSection)
				GATracker.trackEvent("Sections", "Start Section: Link video section");
			else
				GATracker.trackEvent("Sections", "Start Section: Go live section");
		}
		
//		GATracker.trackPageview();
//		showStartOverlay(true);
	}
	

	

	private void initializeYouTubePlayer() {
		YouTubePlayer.loadYouTubeIframeApi();
        YouTubePlayer.addApiReadyHandler(new ApiReadyEventHandler() {

			@Override
			public void onApiReady(ApiReadyEvent event) {
				createYouTubePlayer();
			}
        });
	}
	
	protected void createTVSectionListGallery() {
		VerticalPanel tvSectionContentWrapper = new VerticalPanel();
		tvSectionContentWrapper.getElement().setId("tvSectionContentWrapper");
		tvSectionContentWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		tvSectionContentWrapper.add(embeddedPlayerPanelWrapper);
		TVSectionListGallery tvSectionListGallery = new TVSectionListGallery(View.this, tvServiceImpl);
		tvSectionContentWrapper.add(tvSectionListGallery);
		innerContentPanel.add(tvSectionContentWrapper);
		
//		innerContentPanel.add(embeddedPlayerPanelWrapper);
//		innerContentPanel.add(tvSectionListGallery);
	}


	private void createYouTubePlayer() {
        
		config = (PlayerConfiguration) PlayerConfiguration.createObject();
        config.setHeight(PLAYER_HEIGHT);
        config.setWidth(PLAYER_WIDTH);
       
        playerVars = (PlayerVariables) PlayerVariables.createObject();
//        playerVars.setControls(0);
//        playerVars.setShowInfo(0);
//        playerVars.setDisableKeyboard(1);
//        playerVars.setIvLoadPolicy(3);
//        playerVars.setLoop(0);
          playerVars.setRel(0);
          playerVars.setAutoPlay(0);
          config.setPlayerVars(playerVars);
		
		
        this.tvPlayer = new YouTubePlayer(config);
        this.tvPlayer.getElement().setId("monitorPlayer");
        this.tvPlayer.addPlayerReadyHandler(new PlayerReadyEventHandler() {

            public void onPlayerReady(PlayerReadyEvent event) {
            	if (firstTVSectionBroadcastId != null) {
            		setVideoId(firstTVSectionBroadcastId);
            	}
//            	tvPlayer.getPlayer().playVideo();
//            	monitorPlayer.getPlayer().mute();	
//				monitorPlayer.getPlayer().setVolume(0);
//				setVideoId("");
//                GWT.log("First player is ready.");
//                GWT.log("First player state -> " + monitorPlayer.getPlayer().getPlayerState());
            }
        });		
        
        embeddedPlayerPanel.add(this.tvPlayer);
        embeddedPlayerPanelWrapper.add(embeddedPlayerPanel);
        
        
//        this.monitorPlayer.addStateChangedHandler(new StateChangeEventHandler() {
//
//            public void onStateChange(StateChangeEvent event) {
//            	// Check states: https://developers.google.com/youtube/js_api_reference?csw=1
//            	
////            	if(event.getPlayerEvent().getData() == 0)
////            		catchPause = false;		// Prevents replaying the monitor after the user stopped the video.
////            	if(catchPause && event.getPlayerEvent().getData() == 2)
////            		monitorPlayer.getPlayer().playVideo();
//            	
//            	// If user stopped streaming the monitor player will still show video content because of Youtube's livestreaming latency.
//            	// When the video finished the player will be hidden
//            	if (event.getPlayerEvent().getData() == 0) { 
//        			setMonitorPlayerVisible(false);
//            	}
//                GWT.log("First player state changed => " + event.getPlayerEvent().getData());
//            }
//        });
        
//        this.embeddedPlayerPanel.add(this.player);
//        footerVideoPanel.add(monitorPlayer);
//        setMonitorPlayerVisible(false);
//        catchPause = true;
        
	}

//	public void setMonitorPlayerVisible(boolean visible) {
//		if (!visible) {
//			this.monitorPlayer.getElement().setId("monitorPlayer");
//			monitorPlayer.addStyleName("invisible");
//			monitorPlayer.removeStyleName("redBorder");
//		} else {
//			this.monitorPlayer.getElement().setId("monitorPlayer");
//			monitorPlayer.removeStyleName("invisible");
//			monitorPlayer.addStyleName("redBorder");
//		}
//	}
	
	public void showChoosePieceOverlay(boolean visible) {
		if (visible) {
			RootPanel.get().addStyleName("noscroll");
			overlay = new ChoosePieceOverlay(this, this.serviceImpl, this.serviceImpl.getFormat());
			overlayBack = new HorizontalPanel();
			overlayBack.setStyleName("overlayBack");
			RootPanel.get().add(overlayBack);
			RootPanel.get().add(overlay);
			overlay.setSuggestBoxFocused();
			enableChoosePieceButton(false);
			GATracker.trackEvent("Live streaming flow", "Show \"Choose Piece\" overlay");
		} else {
			overlay.hidePopupPanels();
			RootPanel.get().removeStyleName("noscroll");
			RootPanel.get().remove(overlay);
			RootPanel.get().remove(overlayBack);
			enableChoosePieceButton(true);
		}
	}
	
	public void showGoLiveOverlay(boolean visible) {
		if (visible) {
			RootPanel.get().addStyleName("noscroll");
			goLiveOverlay = new GoLiveOverlay(this, this.serviceImpl, this.serviceImpl.getSelectedPiece());
			overlayBack = new HorizontalPanel();
			overlayBack.setStyleName("overlayBack");
			RootPanel.get().add(overlayBack);
			RootPanel.get().add(goLiveOverlay);
			goLiveOverlay.setOverlayGoLiveButtonFocused();
			GATracker.trackEvent("Live streaming flow", "Show \"Go Live\" overlay");
		} else {
			try {
				RootPanel.get().removeStyleName("noscroll");
				RootPanel.get().remove(goLiveOverlay);
				RootPanel.get().remove(overlayBack);
			} catch (Exception e) {
				// GoLiveOverlay hasn't been created yet
			}
		}
	}
	
	public void showStopStreamOverlay(boolean visible) {
		if (visible) {
			RootPanel.get().addStyleName("noscroll");
			stopStreamOverlay = new StopStreamOverlay(this, this.serviceImpl, this.serviceImpl.getSelectedPiece(), this.broadcastId);
			overlayBack = new HorizontalPanel();
			overlayBack.setStyleName("overlayBack");	
			RootPanel.get().add(overlayBack);
			RootPanel.get().add(stopStreamOverlay);
			GATracker.trackEvent("Live streaming flow", "Stop stream overlay", "show");
		} else {
			RootPanel.get().removeStyleName("noscroll");
			RootPanel.get().remove(stopStreamOverlay);
			RootPanel.get().remove(overlayBack);
		}
		
	}
	
	public void showLinkPieceOverlay(boolean visible) {
		if (visible) {
			button.setEnabled(false);
			RootPanel.get().addStyleName("noscroll");
			linkPieceOverlay = new LinkPieceOverlay(this, this.serviceImpl);
			overlayBack = new HorizontalPanel();
			overlayBack.setStyleName("overlayBack");
			RootPanel.get().add(overlayBack);
			RootPanel.get().add(linkPieceOverlay);
			linkPieceOverlay.setSuggestBoxFocused();
			GATracker.trackEvent("Live streaming flow", "Show \"Choose Piece\" overlay");
		} else {
			button.setEnabled(true);
			linkPieceOverlay.hidePopupPanels();
			RootPanel.get().removeStyleName("noscroll");
			RootPanel.get().remove(linkPieceOverlay);
			RootPanel.get().remove(overlayBack);
		}
	}
	
	private class ChoosePieceButtonHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {

			choosePieceButtonClicked  = true;
			
			if (serviceImpl.getIsCheckingStreamStatus()) {
				serviceImpl.checkStreamStatus(false);
				enableGoLiveButton(true);
				showChoosePieceOverlay(true);
				
				setLiveStatusLabel("offline");
				HTML selectedPieceHTML = new HTML(serviceImpl.getSelectedPiece()); // "\"" + titleOfPiece + "\""
				selectedPieceHTML.setStyleName("selectedPiece");
				setMessageLabelText(new HTML("You chose: </br>" + selectedPieceHTML + "Now click \"Go live!\""),"messagePanel_small", false);
			} else {
//				serviceImpl.instantiateWikiPages();
				showChoosePieceOverlay(true);
			}
			
		}
		
	}
	
	public void createBroadcast() {
		serviceImpl.deleteOldAndCreateNewBroadcast();
	}
	
	private class GoLiveButtonHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			
			if (!serviceImpl.isLive) {
				
				if (isBroadcastCreated) {
//					showGoLiveOverlay(true);
					isAuthenticated = false;
					enableGoLiveButton(false);
					
					Timer t = new Timer() {
						
						@Override
						public void run() {
							if (!isAuthenticated)
								enableGoLiveButton(true);
						}
					};
					t.schedule(3000);	
					
					serviceImpl.checkStreamStatus(true);
				} else {
					isAuthenticated = false;
					enableGoLiveButton(false);
					
					GATracker.trackEvent("Live streaming flow", "User clicked \"Go live\"-button and is prompted to login");
					
					Timer t = new Timer() {
						
						@Override
						public void run() {
							if (!isAuthenticated)
								enableGoLiveButton(true); // vorher: setEnabled...
						}
					};
					t.schedule(3000);	

					createBroadcast();
				}
				
			} else {
				isAuthenticated = false;
				enableGoLiveButton(false);
//				goLiveButton.setStyleName("overlayButtonDisabled");
				setCursorWaiting(true);
				
				Timer t = new Timer() {
					@Override
					public void run() {
						if (!isAuthenticated) {
							enableGoLiveButton(true);
//							goLiveButton.setStyleName("overlayButton");
						}
					}
				};
				t.schedule(3000);	// To make sure the user doesn't click this important button several times in a short time
				
				serviceImpl.stopStreaming();

//				footerVideoGlassPanel.getElement().setId("footerVideoGlassPanel");
//				if (monitorPlayer.getPlayer().getPlayerState() == 3 || monitorPlayer.getPlayer().getPlayerState() == -1) {  
//					// equals: if player is still buffering or unstarted and video has not been played yet
//					checkPlayerStateTimer.cancel();
//					setMonitorPlayerVisible(false);
//				}
			}
		}

	}
	
//	private class ChangeVideoButtonHandler implements ClickHandler {
//		@Override
//		public void onClick(ClickEvent event) {
//			setVideoId("xPp-eeTU6x0");
//		}
//	}
//
//	private class MyUploadsButtonHandler implements ClickHandler {
//		@Override
//		public void onClick(ClickEvent event) {
//			serviceImpl.myUploads();
//		}
//	}
//	
	
//	private class TextBoxCommandChangeHandler implements KeyDownHandler {
//		@Override
//		public void onKeyDown(KeyDownEvent event) {
//			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
//				String command = textBoxCommands.getText();
//				if (command.equals("golive")) 
////					serviceImpl.createBroadcast(); 
//					;
//				else if (command.equals("myuploads"))
//					serviceImpl.myUploads();
//				else if (command.equals("getid"))
//					serviceImpl.getBroadcastId();
//			}
//		}
//	}
	
	public void setVideoId(String id) {
		tvPlayer.getPlayer().loadVideoById(id);
	}
	
	public void updateBroadcastList(List<IdNameTuple> broadcastList) {
		
		int i = 1;
		
		for (IdNameTuple x:broadcastList) {
			
			final String channelName = x.channelName;
			final String id = x.id;
			
			final Anchor anchorName = new Anchor("Live Stream " + i + " (" + channelName + ")");
			anchorName.setStyleName("LiveStreamListLabel");
			anchorName.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setVideoId(id);
				}
			});
			
			liveStreamListPanel.add(anchorName);
			i++;
			
		}
	}
	
	public void enableGoLiveButton(boolean enabled) {
		this.goLiveButton.setEnabled(enabled);
		if (enabled) 
			goLiveButton.setStyleName("overlayButton2");
		else 
			goLiveButton.setStyleName("overlayButtonDisabled2");
	}
	
	public void enableChoosePieceButton(boolean enabled) {
		this.choosePieceButton.setEnabled(enabled);
		if (enabled) 
			choosePieceButton.setStyleName("overlayButton2");
		else 
			choosePieceButton.setStyleName("overlayButtonDisabled2");
	}

	public void setGoLiveButtonText(String text) {
		this.goLiveButton.setText(text);
	}
	
	public void setMessageLabelText(HTML message, String styleName, boolean isAnchor) {
		
		if (isAnchor) {
			this.messagePanel.remove(this.messageLabel);
			this.messagePanel.clear();
			
			this.messageAnchor = new Anchor(message.getText());
			this.messageAnchor.setStyleName(styleName);
			this.messageAnchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Window.open("https://www.youtube.com/my_live_events","_blank","");
				}
			});
			this.messagePanel.add(this.messageAnchor);
			
		} else {
			
			this.messagePanel.remove(this.messageAnchor);
			this.messagePanel.clear();
			message.setStyleName(styleName);
			this.messagePanel.add(message);

		}
		
	}
	
	public void setTextBoxVideoId(String id) {
		this.textBoxVideoId.setText(id);
	}
	
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
	
	public void setGalleryElementIndex(int index) {
		gallery.setIndex(index);
		
		if (index >= 0 && index < ExplanationGallery.GALLERY_ZERO_ELEM_COUNT) {
			setProgressBarElement(0);
			gallery.updateGalleryElement(0);
		}
		
		else if (index >= 1 && index < ExplanationGallery.GALLERY_ONE_ELEM_COUNT + ExplanationGallery.GALLERY_ZERO_ELEM_COUNT) {
			setProgressBarElement(1);
			gallery.updateGalleryElement(1);
		}
		else if (index >= ExplanationGallery.GALLERY_ONE_ELEM_COUNT + ExplanationGallery.GALLERY_ZERO_ELEM_COUNT &&
				index < ExplanationGallery.GALLERY_ONE_ELEM_COUNT + ExplanationGallery.GALLERY_TWO_ELEM_COUNT + ExplanationGallery.GALLERY_ZERO_ELEM_COUNT) {
			setProgressBarElement(2);
			gallery.updateGalleryElement(2);
		}
		else {
			setProgressBarElement(3);
			gallery.updateGalleryElement(3);
		}
	}
	
	public void setProgressBarElement(int i) {
		if (i == 0) {
			if (!serviceImpl.isLive && !this.keepStepTwoDone) {
				gallery.updateGalleryElement(0);
				progressBarElement0.setDone(false);
				progressBarElement1.setDone(false);
				progressBarElement2.setDone(false);
				progressBarElement3.setDone(false);
				progressBarElement0.setMarked(true);
				progressBarElement1.setMarked(false);
				progressBarElement2.setMarked(false);
				progressBarElement3.setMarked(false);
			}
		} else if (i == 1) {
//			gallery.setIndex(0);
			if (!serviceImpl.isLive && !this.keepStepTwoDone) {
				gallery.updateGalleryElement(1);
				progressBarElement0.setDone(true);
				progressBarElement1.setDone(false);
				progressBarElement2.setDone(false);
				progressBarElement3.setDone(false);
				progressBarElement0.setMarked(false);
				progressBarElement1.setMarked(true);
				progressBarElement2.setMarked(false);
				progressBarElement3.setMarked(false);
			}
		} else if (i == 2) {
//			gallery.setIndex(1);
			if (!serviceImpl.isLive && !this.keepStepTwoDone) {
				gallery.updateGalleryElement(2);
				progressBarElement0.setDone(true);
				progressBarElement1.setDone(true);
				progressBarElement2.setDone(false);
				progressBarElement3.setDone(false);
				progressBarElement0.setMarked(false);
				progressBarElement1.setMarked(false);
				progressBarElement2.setMarked(true);
				progressBarElement3.setMarked(false);
			}
		} else if (i == 3) {
//			gallery.setIndex(2);
			gallery.updateGalleryElement(3);
			if (!serviceImpl.isLive) {
				progressBarElement0.setDone(true);
				progressBarElement1.setDone(true);
				progressBarElement2.setDone(true);
				progressBarElement3.setDone(false);
				progressBarElement0.setMarked(false);
				progressBarElement1.setMarked(false);
				progressBarElement2.setMarked(false);
				progressBarElement3.setMarked(true);
			}
		} 
	}
	
	public void setProgressBarDone() {
		
		this.setGalleryElementIndex(11);
		progressBarElement0.setMarked(false);
		progressBarElement2.setMarked(false);
		progressBarElement1.setMarked(false);
		progressBarElement3.setMarked(false);
		progressBarElement0.setDone(true);
		progressBarElement1.setDone(true);
		progressBarElement2.setDone(true);
		progressBarElement3.setDone(true);
	} 
	
	public void setStepTwoDoneAndKeep (boolean keepStepTwoDone) {
		this.keepStepTwoDone = keepStepTwoDone;
		this.setGalleryElementIndex(4);
	}
	
	public void setLiveStatusLabel(String status) {
		this.liveStatusValue = new HTML(status);
		if (serviceImpl.isLive || status.equals("waiting for stream")) {
			this.liveStatusValue.addStyleName("blinking");
		} else
			this.liveStatusValue.removeStyleName("blinking");
		
		if (status.equals("waiting for stream")) {
			this.liveStatusValue.getElement().setId("liveStatusValueWaiting");
		} else if (serviceImpl.isLive) {
			this.liveStatusValue.getElement().setId("liveStatusValueLive");
		} else {
			this.liveStatusValue.getElement().setId("liveStatusValue");
		}
		this.liveStatusPanel.remove(1);
		this.liveStatusPanel.add(this.liveStatusValue);
		
		GATracker.trackEvent("Live status", status);
	}
	
	
	public void setCursorWaiting(boolean waiting) {
		if (waiting) {
			RootPanel.get().getElement().getStyle().setCursor(Cursor.WAIT);
			goLiveButton.getElement().getStyle().setCursor(Cursor.WAIT);
			choosePieceButton.getElement().getStyle().setCursor(Cursor.WAIT);
			try {
				overlay.getCancelButton().getElement().getStyle().setCursor(Cursor.WAIT);
				overlay.getCreateLiveStreamButton().getElement().getStyle().setCursor(Cursor.WAIT);
			} catch (Exception e) {
				// If overlay has not been created yet
			}
			try {
				goLiveOverlay.getOverlayGoLiveButton().getElement().getStyle().setCursor(Cursor.WAIT);
			} catch (Exception e) {
				// If GoLiveOverlay has not been created yet
			}
		} else {
			RootPanel.get().getElement().getStyle().setCursor(Cursor.AUTO);
			goLiveButton.getElement().getStyle().setCursor(Cursor.POINTER);
			choosePieceButton.getElement().getStyle().setCursor(Cursor.POINTER);
			try {
				overlay.getCreateLiveStreamButton().getElement().getStyle().setCursor(Cursor.POINTER);
				overlay.getCancelButton().getElement().getStyle().setCursor(Cursor.POINTER);
			} catch (Exception e) {
				// If overlay has not been created yet
			}
			try {
				goLiveOverlay.getOverlayGoLiveButton().getElement().getStyle().setCursor(Cursor.POINTER);
			} catch (Exception e) {
				// If GoLiveOverlay has not been created yet
			}
		}
	}
	
	/* This important method is called in the serviceImpl's makeBroadcastLive()-RPC-callback and in the StopStreaming()-RPC-callback */
	public void setViewLive(boolean live) {
		if (live) {
//			/* Monitor player */
//			monitorPlayer.getPlayer().mute();
//			monitorPlayer.getPlayer().setVolume(0);
//			setVideoId(this.broadcastId);
//			
//			setMonitorPlayerVisible(true);
//			
//			checkPlayerStateTimer = new Timer() {
//				
//				@Override
//				public void run() {
//					if (monitorPlayer.getPlayer().getPlayerState() == 1) { // equals: if player is playing
//						setMonitorPlayerVisible(true);
//						footerVideoGlassPanel.getElement().setId("footerVideoGlassPanel");
//						this.cancel();
//					}
//				}
//			};
//			checkPlayerStateTimer.scheduleRepeating(5000);
//			
//			footerVideoGlassPanel.getElement().setId("blackBackground");
			
		} else {
			
			
		}
	}
	
	public Button getGoLiveButton() {
		return this.goLiveButton;
	}
	
	public void setIsBroadcastCreated(boolean created) {
		this.isBroadcastCreated = created;
	}
	
	public boolean getIsBroadcastCreated() {
		return this.isBroadcastCreated;
	}
	
	public static void disableTextSelection(Element elem, boolean disable) {
	     setStyleName(elem, "my-no-selection", disable);
	     disableTextSelectInternal(elem, disable);
	}

	 private native static void disableTextSelectInternal(Element e, boolean disable)
		/*-{
	       if (disable) {
	         e.ondrag = function () { return false; };
	         e.onselectstart = function () { return false; };
	       } else {	
	         e.ondrag = null;
	         e.onselectstart = null;
	       }
    }-*/;

	public void setBroadcastId(String myBroadcastId) {
		this.broadcastId = myBroadcastId;
	}

	public void setChoosePieceOverlayControlsEnabled(boolean enabled) {
		if (enabled) {
			overlay.getCancelButton().setEnabled(true);
			overlay.getCancelButton().setStyleName("overlayButton");
			overlay.getCreateLiveStreamButton().setEnabled(true);
			overlay.getCreateLiveStreamButton().setStyleName("overlayButton");
			overlay.getSuggestBox().setEnabled(true);
			overlay.getIsPublicCheckBox().setEnabled(true);
		} else {
			overlay.getCancelButton().setEnabled(false);
			overlay.getCancelButton().setStyleName("overlayButtonDisabled");
			overlay.getCreateLiveStreamButton().setEnabled(false);
			overlay.getCreateLiveStreamButton().setStyleName("overlayButtonDisabled");
			overlay.getSuggestBox().setEnabled(false);
			overlay.getIsPublicCheckBox().setEnabled(false);
		}
	}

	public void setGoLiveOverlayButtonsEnabled(boolean enabled) {
		if (enabled) {
			goLiveOverlay.getCancelButton().setEnabled(true);
			goLiveOverlay.getCancelButton().setStyleName("overlayButton");
			goLiveOverlay.getOverlayGoLiveButton().setEnabled(true);
			goLiveOverlay.getOverlayGoLiveButton().setStyleName("overlayButton");
		} else {
			goLiveOverlay.getCancelButton().setEnabled(false);
			goLiveOverlay.getCancelButton().setStyleName("overlayButtonDisabled");
			goLiveOverlay.getOverlayGoLiveButton().setEnabled(false);
			goLiveOverlay.getOverlayGoLiveButton().setStyleName("overlayButtonDisabled");
		}
	}
	
	public void setStopStreamOverlayButtonsEnabled(boolean enabled) {
		if (enabled) {
			stopStreamOverlay.getOkButton().setEnabled(true);
			stopStreamOverlay.getOkButton().setStyleName("overlayButton");
		} else {
			stopStreamOverlay.getOkButton().setEnabled(false);
			stopStreamOverlay.getOkButton().setStyleName("overlayButtonDisabled");	
		}		
	}


	public void setWikiPages(boolean isForLinkVideoOverlay, List<String> result) {
		if (isForLinkVideoOverlay)
			linkPieceOverlay.setWikiPages(result);
		else
			overlay.setWikiPages(result);
	}

	public void showPieceAcceptedResult(boolean isForLinkVideoOverlay, boolean accepted) {
		if (isForLinkVideoOverlay) 
			linkPieceOverlay.showPieceAcceptedResult(accepted);
		else 
			overlay.showPieceAcceptedResult(accepted);
	}

	public void showFinalPieceCheckResult(boolean isForLinkVideoOverlay, boolean accepted) {
		if (isForLinkVideoOverlay) 
			linkPieceOverlay.showFinalPieceCheckResult(accepted);
		else
			overlay.showFinalPieceCheckResult(accepted);
	}
	
	public void setLinkVideoWikiPages(List<String> result) { // TODO ???
		linkPieceOverlay.setWikiPages(result);
	}

	public void deleteBroadcastFromDatastore(String broadcastId) {
		serviceImpl.deleteBroadcastFromDatastore(broadcastId);
	}


	public void setBroadcastStaysOnIMSLP(boolean stayOnIMSLP, String broadcastId) {
		serviceImpl.setBroadcastStaysOnIMSLP(stayOnIMSLP, broadcastId);	
	}

	public void deleteCompletedBroadcast() {
		serviceImpl.deleteBroadcastCompletely();
	}


	public void showSelectedPieceMessage(String selectedPiece) {
		HTML selectedPieceHTML = new HTML(selectedPiece); // "\"" + titleOfPiece + "\""
		selectedPieceHTML.setStyleName("selectedPiece");
		setMessageLabelText(new HTML("You chose: </br>" + selectedPieceHTML),"messagePanel_small", false); //  + "Now click \"Go live!\""		
	}


	public void setFirstTVSectionBroadcastId(String broadcastId) {
		this.firstTVSectionBroadcastId = broadcastId;
	}


	public void showDeleteVideoOverlay(TVListServiceClientImpl tvServiceImpl, String broadcastTitle, String broadcastId) {
			RootPanel.get().addStyleName("noscroll");
			deleteVideoOverlay = new DeleteVideoOverlay(this, tvServiceImpl, broadcastTitle, broadcastId);
			overlayBack = new HorizontalPanel();
			overlayBack.setStyleName("overlayBack");
			RootPanel.get().add(overlayBack);
			RootPanel.get().add(deleteVideoOverlay);
			deleteVideoOverlay.setCancelButtonFocused();
	}

	public void hideDeleteVideoOverlay() {
		RootPanel.get().removeStyleName("noscroll");
		RootPanel.get().remove(deleteVideoOverlay);
		RootPanel.get().remove(overlayBack);
	}
	
	public void showGoLiveSection(boolean isTracked) {
		Window.scrollTo (0 ,0);
		titlePanel.clear();
		titlePanel.add(titleLabel);
		titlePanel.add(goLiveSubtitleLabel);
		innerContentPanel.clear();
		innerContentPanel.add(titlePanel);
		innerContentPanel.add(controlPanel);
		innerContentPanel.add(galleryPanelWrapper);
		
		tvSectionFooterEntry.removeStyleName("currentFooterEntry");
		goLiveSectionFooterEntry.removeStyleName("currentFooterEntry");
		linkVideosFooterEntry.removeStyleName("currentFooterEntry");
		aboutFooterEntry.removeStyleName("currentFooterEntrySmall");
		goLiveSectionFooterEntry.addStyleName("currentFooterEntry");
		
		if (isTracked)
			GATracker.trackEvent("Sections", "Go Live section");
	}
	
	public void showTVSection(boolean isTracked) {
		Window.scrollTo (0,0);			
		titlePanel.clear();
		titlePanel.add(titleLabel);
		titlePanel.add(tvSectionSubtitleLabel);
		innerContentPanel.clear();
		innerContentPanel.add(titlePanel);
		
		goLiveSectionFooterEntry.removeStyleName("currentFooterEntry");
		linkVideosFooterEntry.removeStyleName("currentFooterEntry");
		aboutFooterEntry.removeStyleName("currentFooterEntrySmall");
		tvSectionFooterEntry.addStyleName("currentFooterEntry");
		
		createTVSectionListGallery();
		
		if (isTracked)
			GATracker.trackEvent("Sections", "TV section");
	}
	
	private void showLinkVideoSection() {
		Window.scrollTo (0 ,0);			
		titlePanel.clear();
		titlePanel.add(titleLabel);
		titlePanel.add(linkVideoSubtitleLabel);
		innerContentPanel.clear();
		innerContentPanel.add(titlePanel);
		
		tvSectionFooterEntry.removeStyleName("currentFooterEntry");
		goLiveSectionFooterEntry.removeStyleName("currentFooterEntry");
		aboutFooterEntry.removeStyleName("currentFooterEntrySmall");
		linkVideosFooterEntry.addStyleName("currentFooterEntry");
		
		// TODO
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		vPanel.setStyleName("sectionContentWrapper");
		
		VerticalPanel innerWrapper = new VerticalPanel();
		innerWrapper.setStyleName("linkVideoInnerWrapper");
		VerticalPanel buttonArea = new VerticalPanel();
		buttonArea.setStyleName("linkVideoButtonArea");
		buttonArea.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		// Copy the url of your Youtube video in the text box
		// Press STRG + v / Command + v to paste the url of your video"
		
//		SearchBox textBox = new SearchBox("Paste your video url here");
		button = new Button("Link video");
		button.setStyleName("linkVideoButton");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showLinkPieceOverlay(true);
			}
		});
		
		VerticalPanel explanationPanel = new VerticalPanel();
		HTML explanationHTML = new HTML(""
				+ "You have classical music performance videos in your Youtube account? </br> "
				+ "You want to get a larger audience for your performances? </br>"
				+ "Then link them with the <a href=\"//imslp.org\" target=\"_blank_\"> Petrucci Music Library </a>! </p> ");
//				+ "<p> Your video can then be watched on the IMSLP page of the classical piece you played. </p> "
		explanationHTML.setStyleName("linkVideoExplanationHTML");
		explanationPanel.add(explanationHTML);
		
		buttonArea.add(explanationPanel);
		buttonArea.add(button);
		
		innerWrapper.add(buttonArea);
		vPanel.add(innerWrapper);
		
		innerContentPanel.add(new HTML("</br>"));
		innerContentPanel.add(vPanel);
		innerContentPanel.add(new HTML("</br> </br>"));
		
		GATracker.trackEvent("Sections", "Link video section");

	}



	public void setProgressBarAfterChosenPiece() {
		if (progressBarElement2.isDone())
			setGalleryElementIndex(3);
		else if (progressBarElement1.isDone())
			setGalleryElementIndex(2);
		else
			setGalleryElementIndex(1);
	}

	public void showLinkPieceSuccess() {
		linkPieceOverlay.showLinkPieceSuccess();
	}

	public void setLinkVideoButtonEnabled(boolean b) {
		linkPieceOverlay.setLinkVideoButtonEnabled(b);
	}
	
}
