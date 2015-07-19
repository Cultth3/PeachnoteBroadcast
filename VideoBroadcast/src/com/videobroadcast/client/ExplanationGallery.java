package com.videobroadcast.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A custom GWT gallery for the explanation of the live streaming process
 * @author Tom
 *
 */


public class ExplanationGallery extends Composite{
	
	public static final int GALLERY_ZERO_ELEM_COUNT = 1;
	public static final int GALLERY_ONE_ELEM_COUNT = 1;
	public static final int GALLERY_TWO_ELEM_COUNT = 1;
	public static final int GALLERY_THREE_ELEM_COUNT = 9;
	public static final int ELEM_COUNT = GALLERY_ZERO_ELEM_COUNT + GALLERY_ONE_ELEM_COUNT + GALLERY_TWO_ELEM_COUNT + GALLERY_THREE_ELEM_COUNT;
	
	private static final int IMAGE_WIDTH = 500;
	private static final int IMAGE_HEIGHT = 225;
	private static final int THUMBNAIL_WIDTH = 32;
	private static final int THUMBNAIL_HEIGHT = 32;
	
	private VerticalPanel galleryPanel = new VerticalPanel();
	private HorizontalPanel imagePanel = new HorizontalPanel();
	private VerticalPanel displayPanel = new VerticalPanel();
	private HorizontalPanel midPanel = new HorizontalPanel();
	private HorizontalPanel thumbnailPanel = new HorizontalPanel();
	private VerticalPanel leftButtonPanel = new VerticalPanel();
	private VerticalPanel rightButtonPanel = new VerticalPanel();
	private VerticalPanel descriptionPanel = new VerticalPanel();
	private HTML description = new HTML();
	private HTML descriptionNote = new HTML();
	
	private FocusPanel leftButtonPanelWrapper;
	private FocusPanel rightButtonPanelWrapper;
	
	private List<GalleryElement> galleryElementsList_0 = new ArrayList<>();
	private List<GalleryElement> galleryElementsList_1 = new ArrayList<>();
	private List<GalleryElement> galleryElementsList_2 = new ArrayList<>();
	private List<GalleryElement> galleryElementsList_3 = new ArrayList<>();
	
	private int index = 0; // currently selected image
	private int oldIndex;	// To reset StyleName of the previous thumbnail
	
	private int iterator = 0; // Just for the anchor iteration
	final private View view;
	private PopupPanel popupPanel;
	private FocusPanel explPanel;
	private boolean popupHidden;


	public ExplanationGallery(View viewParam) {
		initWidget(this.galleryPanel);
		this.view = viewParam;
		
		// Styling
		this.imagePanel.setStyleName("imagePanel");
		this.thumbnailPanel.setStyleName("thumbnailPanel");
		this.midPanel.setStyleName("midPanel");
		this.displayPanel.setStyleName("displayPanel");
		this.leftButtonPanel.setStyleName("galleryButtonPanelLeft");
		this.rightButtonPanel.setStyleName("galleryButtonPanelRight");
		this.rightButtonPanel.addStyleName("blinkingSmoothly");
		this.descriptionPanel.setStyleName("descriptionPanel");
		this.description.setStyleName("description");
		this.descriptionNote.setStyleName("descriptionNote");
		
		galleryPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		galleryPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		midPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		
		rightButtonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rightButtonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		leftButtonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		leftButtonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		// Initialisations and composition
//		leftArrow = new Button(" << ");
//		rightArrow = new Button(" >> ");
//		leftButtonLabel = new Label(" step backward ");
//		rightButtonLabel = new Label(new HTML("huskadgh").getText());
//		Image leftArrow = new Image(Resources.INSTANCE.ArrowLeft().getSafeUri());
//		Image rightArrow = new Image(Resources.INSTANCE.ArrowRight().getSafeUri());
		Image leftArrow = new Image("http://i.imgur.com/o10MIUM.png");
		Image rightArrow = new Image("http://i.imgur.com/XT9kt7q.png");
		leftArrow.setPixelSize(55, 72);
		rightArrow.setPixelSize(55, 72);
		
		View.disableTextSelection(leftButtonPanel.getElement(), true);
		View.disableTextSelection(rightButtonPanel.getElement(), true);
		View.disableTextSelection(leftArrow.getElement(), true);
		View.disableTextSelection(rightArrow.getElement(), true);
		leftArrow.setStyleName("unselectable");
		rightArrow.setStyleName("unselectable");
		
		
//		leftButtonLabel.setWordWrap(false);
		this.leftButtonPanel.add(leftArrow);
		this.rightButtonPanel.add(rightArrow);
		
		this.leftButtonPanelWrapper = new FocusPanel(leftButtonPanel);
		this.rightButtonPanelWrapper = new FocusPanel(rightButtonPanel);
		leftButtonPanelWrapper.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (index >= 1) { // TODO: 4 elements: 1; 3 elements: 2
					oldIndex = index;
					index--;
					view.setGalleryElementIndex(index);
					GATracker.trackEvent("Gallery", "Gallery buttons", "Click backward to element " + index);
					GATracker.trackEvent("Gallery", "Gallery buttons", "Click backward");
				}
			}
		});
		rightButtonPanelWrapper.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (index < ELEM_COUNT - 1) {
					oldIndex = index;
					index++;
					view.setGalleryElementIndex(index);
					GATracker.trackEvent("Gallery", "Gallery buttons", "Click forward to element " + index);
					GATracker.trackEvent("Gallery", "Gallery buttons", "Click forward");
				}
			}
		});
		
		this.descriptionPanel.add(description);
//		this.descriptionPanel.add(descriptionNote);
		this.displayPanel.add(descriptionPanel);
		this.midPanel.add(leftButtonPanelWrapper);
		this.midPanel.add(imagePanel);
		this.midPanel.add(rightButtonPanelWrapper);
		this.displayPanel.add(midPanel);
		this.galleryPanel.add(displayPanel);
		this.galleryPanel.add(thumbnailPanel);

		
		loadGalleryItems();
		
		// Fill ThumbnailPanel with first gallery
		for (GalleryElement elem : this.galleryElementsList_3) {
			this.thumbnailPanel.add(elem.getAnchor());
//			Window.alert("" + elem.getAnchor());
		}
		

		explPanel = new FocusPanel();
		explPanel.setStyleName("descriptionExplanationOverlay");
		HorizontalPanel explImageWrapper = new HorizontalPanel();
		explImageWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		explImageWrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		explImageWrapper.setStyleName("explImageWrapper");
//		Image explanationImage = new Image(Resources.INSTANCE.explanation().getSafeUri());
		Image explanationImage = new Image("http://i.imgur.com/0bZ4ie9.png");
		explanationImage.setPixelSize(22,22);
		explImageWrapper.add(explanationImage);
		explPanel.add(explImageWrapper);
		popupPanel = new PopupPanel();
		
		popupPanel = new PopupPanel();
		HorizontalPanel childPanel = new HorizontalPanel();
		
		childPanel.add(new HTML("<em> For Youtube experts: </br>"
				+ " By clicking \"Go live\" a broadcast event and a stream"
				+ " will be created in your account and bound together. "
				+ " So you don't have to create and set up anything in your account."
				+ " This application makes the creation of broadcasts "
				+ " way faster than you could do it in your Youtube account. "
				+ " If you want to change the title or settings"
				+ " of your video, you still can do it after the livestream."
				+ " So this application doesn't prevent you from changing any setting of your video. </em> "));
		childPanel.setStyleName("popupPanelChild");
		popupPanel.setStyleName("popupPanel");
//				popupPanel.addStyleName("opacity");
		popupPanel.add(childPanel);
		popupPanel.setAnimationEnabled(true);
		
		popupHidden = true;
		
//		explanationImage.addMouseOutHandler(new MouseOutHandler() {
//			
//			@Override
//			public void onMouseOut(MouseOutEvent event) {
//				Timer t = new Timer() {
//					
//					@Override
//					public void run() {
//						popupPanel.hide();
//						popupHidden = true;
//					}
//				};
//				t.schedule(2000);
//			}
//		});
		
//		explanationImage.addMouseOverHandler(new MouseOverHandler() {
//			
//			@Override
//			public void onMouseOver(MouseOverEvent event) {
//				
//				if (popupHidden) {
//					
//					popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
//						@Override
//						public void setPosition(int offsetWidth, int offsetHeight) {
//							popupPanel.setPopupPosition(explPanel.getAbsoluteLeft() + 35, explPanel.getAbsoluteTop() - 10);
//						}
//					});
//					
//					popupHidden = false;
//				}
//			}
//		});
		
//		explanationImage.addClickHandler(new ClickHandler() {
//			
//			@Override
//			public void onClick(ClickEvent event) {
//				popupPanel.hide();
//				popupHidden = true;
//			}
//		});
		
		MouseOverHandler cHandler = new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				
//				if (popupHidden) {
					
					popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							popupPanel.setPopupPosition(explPanel.getAbsoluteLeft() + 15, explPanel.getAbsoluteTop() - 10);
						}
					});
					
//					popupHidden = false;
//				}
			}
		};
		
		
		
		
		MouseOutHandler vc = new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				popupPanel.hide();
//				Timer t = new Timer() {
//					
//					@Override
//					public void run() {
//						popupHidden = true;
//					}
//				};
//				t.schedule(500);
			}
		};
		
		ClickHandler vcClick = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				popupPanel.hide();
			}
		};
		
		explanationImage.addDomHandler(cHandler, MouseOverEvent.getType());
//		explanationImage.addDomHandler(vc, MouseOutEvent.getType());
		childPanel.addDomHandler(vc, MouseOutEvent.getType());
		childPanel.addDomHandler(vcClick, ClickEvent.getType());
//		checkIndex();
		
	}
	
	
	
	/*
	 * Important method, that puts all gallery images, thumbnails and
	 * descriptions in lists and builds galleryElements with their contents
	 */
	private void loadGalleryItems() {
		
		List<Image> imageList = new ArrayList<>();
		List<Image> thumbnailList = new ArrayList<>();	// Thumbnail array
		List<String> descriptionList = new ArrayList<>();
//		List<String> descriptionNoteList = new ArrayList<>();
		
		// Load description list (order matters and must be corresponding to the imageList's order)
		String d0 = "<p> <span class=\"descriptionBigFont\"> </br> Click <em> Choose piece </em> on the bottom of the page </br> and type in the piece you want to play! </span> </br>";
		String d1 = "<p> <span class=\"descriptionBigFont\"> Click on the image below to download and install <em> Wirecast </em> </span> </br>"
				+ "Wirecast is necessary to send the signal of your webcam to Youtube.  </br> "
				+ "After installing click \"next\"! </p> "; // <span class=\"descriptionNote\"> </span> 
		String d2 = "<p> <span class=\"descriptionBigFont\"> </br> Click on the image to login to Youtube/create a Youtube/Google account </br> and to enable live streaming in your account. <span> </br>"
				+ "</p>"; // <span class=\"descriptionNote\"> Consider: Youtube will ask you to verify your account by phone. </span> 
//		String d3 = "<p> <span class=\"descriptionBigFont\"> </br> Click <em> Choose piece </em> on the bottom of the page </br> and type in the piece you want to play! </span> </br>";
//				+ "</p> ";  // <span class=\"descriptionNote\"> Consider: This application will ask you to grant access to your Youtube account </span> 
		String d4 = "<p> <span class=\"descriptionBigFont\"> Click <em> Go live! </em> </span> </br> Consider: The first time a popup window will open and ask you to grant access </br> to your Youtube account <span style=\"color:#dd0000;\"> (disable pop-up blockers if it doesn't show up) </span> </p> "; // <span class=\"descriptionNote\"> </span> 
		String d5 = "<p> <span class=\"descriptionBigFont\"> </br> </br> Now open Wirecast and select <em> Output -> Output Settings... </em> </span> </p> "; // style=\"margin-top: 20px;\"
		String d6 = "<p> <span class=\"descriptionBigFont\"> </br> Click on <em> Authenticate </em> </span> </br>  Consider: Your browser will open and ask you to login to your Youtube account. </p>"; // <span class=\"descriptionNote\"> </span> 
		String d7 = "<p> <span class=\"descriptionBigFont\"> Login to the <em> same account </em>  again and give Wirecast access to your account </span> " // <span style=\"color: #ffdddd;\">
				+ " Important: If you have more channels in your account, select the channel you have been logged in " // <span class=\"descriptionNote\"> </span> 
				+ "when you clicked the <em> Go live! </em>-button </p>  ";
		String d8 = "<p> <span class=\"descriptionBigFont\"> </br>  Select the latest (topmost) entry with the title </br> of your chosen piece in the events list "  // This is the stream you created by clicking <em> Create Livestream. </em>
				+ " and close the output settings </span> </p> ";
		String d9 = "<p> <span class=\"descriptionBigFont\"> </br> </br> Choose your camera as source for the Wirecast output </span> </p> ";
		String d10 = "<p> <span class=\"descriptionBigFont\"> </br>  Activate the <em>Stream</em> - button </br> "
				+ "Important: If it was already active you have to click it twice!" // <span class=\"descriptionNote\"> </span>  
				+ "  </span> </p> ";
				// <span class=\"descriptionNote\">	Now Wirecast is sending the stream of your webcam to the broadcast event in your Youtube account that was created by clicking <em> Create Livestream </em>. </span>
		String d11 = "<p> <span class=\"descriptionBigFont\"> </br> When the stream comes in confirm the upcoming dialogue </br> and play the piece you have chosen! </span> </p> ";
		String d12 = "<p> <span class=\"descriptionBigFont\"> When you finished playing click <em> Stop! </em> </br> </span> In a dialogue you can decide if you want to keep the video or delete it. </br> Watch your interpretation on IMSLP or in the TV section of this page! </p> ";
		
		descriptionList.add(d0);
		descriptionList.add(d1);
		descriptionList.add(d2);
//		descriptionList.add(d3);
		descriptionList.add(d4);
		descriptionList.add(d5);
		descriptionList.add(d6);
		descriptionList.add(d7);
		descriptionList.add(d8);
		descriptionList.add(d9);
		descriptionList.add(d10);
		descriptionList.add(d11);
		descriptionList.add(d12);
		
		
//		// Load descriptionNote list (order matters and must be corresponding to the imageList's order)
//		String dn1 = "After every step press the \"next\"-button! ";
//		String dn2 = "Consider: Youtube will ask you to verify your account by phone.";
//		String dn3 = "Consider: OMG3333!!!!";
////		String dn4 = "";
//		String dn5 = "";
//		String dn6 = "";
//		String dn7 = "";
//		String dn8 = "";
//		String dn9 = "";
//		String dn10 = "";
//		String dn11 = "";
//		String dn12 = "";
//		
//		descriptionNoteList.add(dn1);
//		descriptionNoteList.add(dn2);
//		descriptionNoteList.add(dn3);
////		descriptionNoteList.add(dn4);
//		descriptionNoteList.add(dn5);
//		descriptionNoteList.add(dn6);
//		descriptionNoteList.add(dn7);
//		descriptionNoteList.add(dn8);
//		descriptionNoteList.add(dn9);
//		descriptionNoteList.add(dn10);	
//		descriptionNoteList.add(dn11);
//		descriptionNoteList.add(dn12);
				
		// Load image list
//		Image image0 = new Image(Resources.INSTANCE.image_ClickChoosePieceButton().getSafeUri());
//		Image image1 = new Image(Resources.INSTANCE.image_DownloadWirecast().getSafeUri());
//		Image image2 = new Image(Resources.INSTANCE.image_EnableLivestreaming().getSafeUri());
//		Image image3 = new Image(Resources.INSTANCE.image_ClickChoosePieceButton().getSafeUri());
//		Image image4 = new Image(Resources.INSTANCE.image_ClickGoLiveButton().getSafeUri());
//		Image image5 = new Image(Resources.INSTANCE.image_WOutputSettings().getSafeUri());
//		Image image6 = new Image(Resources.INSTANCE.image_WAuthenticate().getSafeUri());
//		Image image7 = new Image(Resources.INSTANCE.image_AllowWirecastAccess().getSafeUri());
//		Image image8 = new Image(Resources.INSTANCE.image_WSelectEvent().getSafeUri());
//		Image image9 = new Image(Resources.INSTANCE.image_WActivateCamera().getSafeUri());
//		Image image10 = new Image(Resources.INSTANCE.image_WActivateStreamButton().getSafeUri());
//		Image image11 = new Image(Resources.INSTANCE.image_GoLiveOverlay().getSafeUri());
//		Image image12 = new Image(Resources.INSTANCE.image_ClickStopStreamButton().getSafeUri());

		Image image0 = new Image("http://i.imgur.com/qB0DpbW.jpg");
		Image image1 = new Image("http://i.imgur.com/7Ny7tZ0.jpg");
		Image image2 = new Image("http://i.imgur.com/AK3mUNR.jpg");
		Image image3 = new Image("http://i.imgur.com/qB0DpbW.jpg");
		Image image4 = new Image("http://i.imgur.com/4W4f0Gt.jpg");
		Image image5 = new Image("http://i.imgur.com/LZxRo3K.jpg");
		Image image6 = new Image("http://i.imgur.com/1vskDKS.jpg");
		Image image7 = new Image("http://i.imgur.com/lvV2APT.jpg");
		Image image8 = new Image("http://i.imgur.com/1Cz3t1J.jpg");
		Image image9 = new Image("http://i.imgur.com/9VcfcmZ.jpg");
		Image image10 = new Image("http://i.imgur.com/9c0Kpgw.jpg");
		Image image11 = new Image("http://i.imgur.com/5J3G5BC.jpg");
		Image image12 = new Image("http://i.imgur.com/QoaqfcU.jpg");
		
		image0.setStyleName("galleryImage");
		image1.setStyleName("galleryImage");
		image2.setStyleName("galleryImage");
		image3.setStyleName("galleryImage");
		image4.setStyleName("galleryImage");
		image5.setStyleName("galleryImage");
		image6.setStyleName("galleryImage");
		image7.setStyleName("galleryImage");
		image8.setStyleName("galleryImage");
		image9.setStyleName("galleryImage");
		image10.setStyleName("galleryImage");
		image11.setStyleName("galleryImage");
		image12.setStyleName("galleryImage");
		
//		image1.setUrl("http://www.telestream.net/wirecastforyoutube/cb-landing.htm");
		image1.getElement().getStyle().setCursor(Cursor.POINTER);
		image1.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("http://www.telestream.net/wirecastforyoutube/cb-landing.htm", "_blank", "");
				GATracker.trackEvent("Gallery", "Gallery image clicks", "Download Wirecast");
			}
		});
		
		image2.getElement().getStyle().setCursor(Cursor.POINTER);
		image2.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("https://www.youtube.com/my_live_events", "_blank", "");
				GATracker.trackEvent("Gallery", "Gallery image clicks", "Enable live streaming");
			}
		});
		
		image0.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image1.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image2.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image3.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image4.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image5.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image6.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image7.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image8.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image9.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image10.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image11.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		image12.setPixelSize(IMAGE_WIDTH, IMAGE_HEIGHT);
		
		
		// (order matters!)
		imageList.add(image0);
		imageList.add(image1);
		imageList.add(image2);
//		imageList.add(image3);
		imageList.add(image4);
		imageList.add(image5);
		imageList.add(image6);
		imageList.add(image7);
		imageList.add(image8);
		imageList.add(image9);
		imageList.add(image10);
		imageList.add(image11);
		imageList.add(image12);
		
		
		
		// Load Thumbnail list
//		Image image0Tn = new Image(Resources.INSTANCE.image_ClickChoosePieceButton().getSafeUri());
//		Image image1Tn = new Image(Resources.INSTANCE.image_DownloadWirecast().getSafeUri());
//		Image image2Tn = new Image(Resources.INSTANCE.image_EnableLivestreaming().getSafeUri());
//		Image image3Tn = new Image(Resources.INSTANCE.image_ClickChoosePieceButton().getSafeUri());
//		Image image4Tn = new Image(Resources.INSTANCE.image_ClickGoLiveButton().getSafeUri());
//		Image image5Tn = new Image(Resources.INSTANCE.image_WOutputSettings().getSafeUri());
//		Image image6Tn = new Image(Resources.INSTANCE.image_WAuthenticate().getSafeUri());
//		Image image7Tn = new Image(Resources.INSTANCE.image_AllowWirecastAccess().getSafeUri());
//		Image image8Tn = new Image(Resources.INSTANCE.image_WSelectEvent().getSafeUri());
//		Image image9Tn = new Image(Resources.INSTANCE.image_WActivateCamera().getSafeUri());
//		Image image10Tn = new Image(Resources.INSTANCE.image_WActivateStreamButton().getSafeUri());
//		Image image11Tn = new Image(Resources.INSTANCE.image_GoLiveOverlay().getSafeUri());
//		Image image12Tn = new Image(Resources.INSTANCE.image_ClickStopStreamButton().getSafeUri());
		
		Image image0Tn = new Image("http://i.imgur.com/qB0DpbW.jpg");
		Image image1Tn = new Image("http://i.imgur.com/7Ny7tZ0.jpg");
		Image image2Tn = new Image("http://i.imgur.com/AK3mUNR.jpg");
		Image image3Tn = new Image("http://i.imgur.com/qB0DpbW.jpg");
		Image image4Tn = new Image("http://i.imgur.com/4W4f0Gt.jpg");
		Image image5Tn = new Image("http://i.imgur.com/LZxRo3K.jpg");
		Image image6Tn = new Image("http://i.imgur.com/1vskDKS.jpg");
		Image image7Tn = new Image("http://i.imgur.com/lvV2APT.jpg");
		Image image8Tn = new Image("http://i.imgur.com/1Cz3t1J.jpg");
		Image image9Tn = new Image("http://i.imgur.com/9VcfcmZ.jpg");
		Image image10Tn = new Image("http://i.imgur.com/9c0Kpgw.jpg");
		Image image11Tn = new Image("http://i.imgur.com/5J3G5BC.jpg");
		Image image12Tn = new Image("http://i.imgur.com/QoaqfcU.jpg");

		image0Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image1Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image2Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image3Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image4Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image5Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image6Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image7Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image8Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image9Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image10Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image11Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		image12Tn.setPixelSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		
		// (order matters! It must be identical to the order of the other lists)
		thumbnailList.add(image0Tn);
		thumbnailList.add(image1Tn);
		thumbnailList.add(image2Tn);
//		thumbnailList.add(image3Tn);
		thumbnailList.add(image4Tn);
		thumbnailList.add(image5Tn);
		thumbnailList.add(image6Tn);
		thumbnailList.add(image7Tn);
		thumbnailList.add(image8Tn);
		thumbnailList.add(image9Tn);
		thumbnailList.add(image10Tn);
		thumbnailList.add(image11Tn);
		thumbnailList.add(image12Tn);
			
		// Bundle everything in GalleryElements for gallery 0
		for (int i=0; i < GALLERY_ZERO_ELEM_COUNT; i++) {
			
			final GalleryElement elem = new GalleryElement();
			elem.setElementNumber(i);
			elem.setDescription((descriptionList.get(i)));
//					elem.setDescriptionNote(descriptionNoteList.get(i));
			elem.setImage(imageList.get(i));
			elem.setThumbnail(thumbnailList.get(i));
			
			final Anchor anchor = new Anchor();
			
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					oldIndex = index;
					index = elem.getElementNumber();
//							checkIndex();
				}
			});
			
			anchor.getElement().getStyle().setCursor(Cursor.POINTER);
			anchor.getElement().appendChild(elem.getThumbnail().getElement());
			
			elem.setAnchor(anchor);
			
			galleryElementsList_0.add(elem);
			
		}
		
		// Bundle everything in GalleryElements for gallery 1
		for (int i=0; i < GALLERY_ONE_ELEM_COUNT; i++) {
			
			int listIndex = i + GALLERY_ZERO_ELEM_COUNT;
			
			final GalleryElement elem = new GalleryElement();
			elem.setElementNumber(listIndex);
			elem.setDescription((descriptionList.get(listIndex)));
//			elem.setDescriptionNote(descriptionNoteList.get(i));
			elem.setImage(imageList.get(listIndex));
			elem.setThumbnail(thumbnailList.get(listIndex));
			
			
			final Anchor anchor = new Anchor();
			
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					oldIndex = index;
					index = elem.getElementNumber();
//					checkIndex();
				}
			});
			
			anchor.getElement().getStyle().setCursor(Cursor.POINTER);
			anchor.getElement().appendChild(elem.getThumbnail().getElement());
			
			elem.setAnchor(anchor);
			
			galleryElementsList_1.add(elem);
			
		}
		
		

		// Bundle everything in GalleryElements for gallery 3
		for (int i=0; i < GALLERY_THREE_ELEM_COUNT; i++) {
			
			int listIndex = i + GALLERY_ONE_ELEM_COUNT + GALLERY_TWO_ELEM_COUNT + GALLERY_ZERO_ELEM_COUNT;
			
			final GalleryElement elem = new GalleryElement();
			elem.setElementNumber(listIndex);
			elem.setDescription((descriptionList.get(listIndex)));
//			elem.setDescriptionNote(descriptionNoteList.get(listIndex));
			elem.setImage(imageList.get(listIndex));
			elem.setThumbnail(thumbnailList.get(listIndex));
			
			
			final Anchor anchor = new Anchor();
			
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					oldIndex = index;
					index = elem.getElementNumber();
					view.setGalleryElementIndex(index);
				}
			});
			
			anchor.getElement().getStyle().setCursor(Cursor.POINTER);
			anchor.getElement().appendChild(elem.getThumbnail().getElement());
			
			elem.setAnchor(anchor);
			
			galleryElementsList_3.add(elem);
			
		}
		
		
		// Bundle everything in GalleryElements for gallery 2
		for (int i=0; i < GALLERY_TWO_ELEM_COUNT; i++) {
			
			int listIndex = i + GALLERY_ONE_ELEM_COUNT + GALLERY_ZERO_ELEM_COUNT;
			
			final GalleryElement elem = new GalleryElement();
			elem.setElementNumber(listIndex);
			elem.setDescription((descriptionList.get(listIndex)));
//			elem.setDescriptionNote(descriptionNoteList.get(listIndex));
			elem.setImage(imageList.get(listIndex));
			elem.setThumbnail(thumbnailList.get(listIndex));
			
			
			final Anchor anchor = new Anchor();
			
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					oldIndex = index;
					index = elem.getElementNumber();
//					checkIndex();
				}
			});
			
			anchor.getElement().getStyle().setCursor(Cursor.POINTER);
			anchor.getElement().appendChild(elem.getThumbnail().getElement());
			
			elem.setAnchor(anchor);
//			Window.alert("2: " + elem.getAnchor());
			
			
			galleryElementsList_2.add(elem);
			
		}
			
		
		
	}
	
	// is called by view
	public void updateGalleryElement(int progressBarElement) {
		
		GalleryElement elem = null;
		
		if (progressBarElement == 0) {
			elem = this.galleryElementsList_0.get(index);
			thumbnailPanel.clear();
			for (GalleryElement galleryElement : galleryElementsList_0) {
				galleryElement.getThumbnail().setStyleName("thumbnail");
				thumbnailPanel.add(galleryElement.getAnchor());
			}
		} else if (progressBarElement == 1) {
			int listIndex = index - GALLERY_ZERO_ELEM_COUNT;
			elem = this.galleryElementsList_1.get(listIndex);
			thumbnailPanel.clear();
			for (GalleryElement galleryElement : galleryElementsList_1) {
				galleryElement.getThumbnail().setStyleName("thumbnail");
				thumbnailPanel.add(galleryElement.getAnchor());
			}
		} else if (progressBarElement == 2) {
			int listIndex = index - GALLERY_ZERO_ELEM_COUNT - GALLERY_ONE_ELEM_COUNT;
			elem = this.galleryElementsList_2.get(listIndex);
			thumbnailPanel.clear();
			for (GalleryElement galleryElement : galleryElementsList_2) {
				galleryElement.getThumbnail().setStyleName("thumbnail");
				thumbnailPanel.add(galleryElement.getAnchor());
			}
		} else if (progressBarElement == 3) {
			int listIndex = index - GALLERY_ZERO_ELEM_COUNT - GALLERY_ONE_ELEM_COUNT - GALLERY_TWO_ELEM_COUNT;
			elem = this.galleryElementsList_3.get(listIndex);
			thumbnailPanel.clear();
			for (GalleryElement galleryElement : galleryElementsList_3) {
				galleryElement.getThumbnail().setStyleName("thumbnail");
				thumbnailPanel.add(galleryElement.getAnchor());
//				Window.alert("" + galleryElement.getAnchor());
			}
		}
		
		this.imagePanel.clear();
		this.imagePanel.add(elem.getImage());
		elem.getThumbnail().setStyleName("selectedThumbnail");
		this.description.setHTML(elem.getDescription());
		
		
		if (index == ELEM_COUNT - 1) {
			this.rightButtonPanel.removeStyleName("blinkingSmoothly");
			this.rightButtonPanel.addStyleName("galleryButtonPanelEnd");
		} else {
			this.rightButtonPanel.removeStyleName("galleryButtonPanelEnd");
			this.rightButtonPanel.addStyleName("blinkingSmoothly");
		}
		
		if (index == 0) { // 4 elements: 0; 3 elements: 1
			this.leftButtonPanel.addStyleName("galleryButtonPanelEnd");
		} else {
			this.leftButtonPanel.removeStyleName("galleryButtonPanelEnd");
		}
		
		if (index == 3) {
			this.descriptionPanel.add(explPanel);
		} else { 
			try {
				this.descriptionPanel.remove(explPanel);
			} catch (Exception e) {}
		}
		
//		GATracker.trackEvent("Progress Bar", "Gallery element", "General count for index " + index);
		
//		this.descriptionNote.setHTML(elem.getDescriptionNote());
		
	}
	
	public void setIndex(int i) {
		index = i;
	}
	
}
