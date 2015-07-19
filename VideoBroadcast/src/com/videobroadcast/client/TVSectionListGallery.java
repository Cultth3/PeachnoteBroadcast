package com.videobroadcast.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.videobroadcast.shared.BroadcastInfo;

/**
 * Representation of the list of all Youtube videos linked with IMSLP for the TV
 * section
 * 
 * @author Tom
 *
 */

public class TVSectionListGallery extends Composite {
	
	private static final int GALLERY_ELEMENTS_NUMBER = 4;

	private View mainView;
	private TVListServiceClientImpl tvServiceImpl;
	
	private VerticalPanel mainPanel = new VerticalPanel();
	private VerticalPanel leftButtonPanel = new VerticalPanel();
	private VerticalPanel rightButtonPanel = new VerticalPanel();
	private FocusPanel leftButtonPanelWrapper;
	private FocusPanel rightButtonPanelWrapper;	
	private HorizontalPanel searchPanelWrapper = new HorizontalPanel();
	private VerticalPanel searchPanel = new VerticalPanel(); 
	private HorizontalPanel buttonWrapper = new HorizontalPanel();
	private HorizontalPanel tvGalleryPanel = new HorizontalPanel();
	private HorizontalPanel tvGalleryElementsPanel = new HorizontalPanel();
	private VerticalPanel tvGalleryCenterPanel = new VerticalPanel();
	private HTML leftButtonPanelHTML;
	private HTML rightButtonPanelHTML;
	
	private TVSectionListGalleryElement[] galleryElements;
	private List<BroadcastInfo> broadcastInfoList;
	private boolean isEndOfList = false;
	int index = 0;
	private SearchBox searchBox;

	public TVSectionListGallery(View mainView, final TVListServiceClientImpl tvServiceImpl) {
		initWidget(this.mainPanel);
		this.mainView = mainView;
		this.tvServiceImpl = tvServiceImpl;
		this.tvServiceImpl.setTVSectionListGallery(this);
		
		this.getElement().setId("tvSectionListGallery");
		
		/* Gallery buttons */
		View.disableTextSelection(leftButtonPanel.getElement(), true);
		View.disableTextSelection(rightButtonPanel.getElement(), true);
		
		leftButtonPanelHTML = new HTML();
		rightButtonPanelHTML = new HTML();
		leftButtonPanelHTML.setStyleName("tvSectionListGalleryLeftButtonPanelHTMLEnd");
		rightButtonPanelHTML.setStyleName("tvSectionListGalleryRightButtonPanelHTML");
		this.leftButtonPanel.add(leftButtonPanelHTML);
		this.rightButtonPanel.add(rightButtonPanelHTML);
		
		this.leftButtonPanelWrapper = new FocusPanel(leftButtonPanel);
		this.rightButtonPanelWrapper = new FocusPanel(rightButtonPanel);
		leftButtonPanelWrapper.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (index >= 1) {
					index--;
					isEndOfList = false;
					updateGalleryElements();
					rightButtonPanelHTML.setStyleName("tvSectionListGalleryRightButtonPanelHTML");
					if (index == 0) 
						leftButtonPanelHTML.setStyleName("tvSectionListGalleryLeftButtonPanelHTMLEnd");
				}
			}
		});
		rightButtonPanelWrapper.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!isEndOfList) {
					index++;
					updateGalleryElements();
					leftButtonPanelHTML.setStyleName("tvSectionListGalleryLeftButtonPanelHTML");
				} else {
					rightButtonPanelHTML.setStyleName("tvSectionListGalleryRightButtonPanelHTMLEnd");
				}
			}
		});
	
		tvGalleryElementsPanel.getElement().setId("tvGalleryElementsPanel");
		tvGalleryCenterPanel.getElement().setId("tvGalleryCenterPanel");
		buttonWrapper.getElement().setId("tvGallerySearchPanelButtonWrapper");
		searchPanelWrapper.getElement().setId("tvGallerySearchPanelWrapper");
		searchPanel.getElement().setId("tvGallerySearchPanel");
		tvGalleryPanel.getElement().setId("tvGalleryPanel");
	
		
		tvGalleryElementsPanel.getElement().getStyle().setWidth(100.0, Style.Unit.PX);
		tvGalleryElementsPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		tvGalleryElementsPanel.getElement().getStyle().setDisplay(Display.TABLE);
		tvGalleryPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		
		galleryElements = new TVSectionListGalleryElement[GALLERY_ELEMENTS_NUMBER];

		for (int i=0; i < GALLERY_ELEMENTS_NUMBER; i++) {
			galleryElements[i] = new TVSectionListGalleryElement(mainView, tvServiceImpl);
			tvGalleryElementsPanel.add(galleryElements[i]);
		}
		
		tvGalleryCenterPanel.add(tvGalleryElementsPanel);
		
		tvGalleryPanel.add(leftButtonPanelWrapper);
		tvGalleryPanel.add(tvGalleryCenterPanel);
		tvGalleryPanel.add(rightButtonPanelWrapper);
		
		searchBox = new SearchBox("Search for channel or piece", "tvGallerySearchPanelTextBoxDefault", "tvGallerySearchPanelTextBox");

		final Button refreshButton = new Button("Refresh list");
		refreshButton.getElement().setId("tvGallerySearchPanelButton");
		refreshButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
					tvServiceImpl.requestVideoList(searchBox.getText());
			}
		});

		searchBox.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					refreshButton.click();
				}
			}
		});
		
		
		buttonWrapper.add(refreshButton);
		searchPanel.add(searchBox);
		searchPanel.add(buttonWrapper);
		searchPanelWrapper.add(searchPanel);
		
		mainPanel.add(tvGalleryPanel);
		mainPanel.add(searchPanelWrapper);
		
		broadcastInfoList = new ArrayList<BroadcastInfo>();
		
		requestVideoList();
		
	}
	
	public void requestVideoList() {
		tvServiceImpl.requestVideoList("");
	}
	
	public void setBroadcastInfoList(List<BroadcastInfo> broadcastInfoList) {
		this.broadcastInfoList = broadcastInfoList;
		if (this.broadcastInfoList.size() > GALLERY_ELEMENTS_NUMBER) {
			setIsEndOfList(false);
		}
	}
	
	public void updateGalleryElements() {
		
		for (int i=0; i < galleryElements.length; i++) {
			galleryElements[i].clearContent();

			try {
				BroadcastInfo info = broadcastInfoList.get(i + index*GALLERY_ELEMENTS_NUMBER);
				galleryElements[i].activateGalleryElement(true);
				galleryElements[i].updateContent(info.getBroadcastId(), info.getBroadcastTitle(), info.getChannelName(), info.getLifeCycleStatus(), info.getChannelId());
				
				if (broadcastInfoList.size() == GALLERY_ELEMENTS_NUMBER + index*GALLERY_ELEMENTS_NUMBER && i == GALLERY_ELEMENTS_NUMBER - 1) 
					setIsEndOfList(true);
			} catch (Exception e) {
				/* end of list is reached */
				setIsEndOfList(true);
				galleryElements[i].activateGalleryElement(false);
			}
				
			
		}
		
		mainView.setFirstTVSectionBroadcastId(broadcastInfoList.get(0).getBroadcastId());
			
	}

	private void setIsEndOfList(boolean isEndOfList) {
		if (isEndOfList) {
			this.isEndOfList = true;
			rightButtonPanelHTML.setStyleName("tvSectionListGalleryRightButtonPanelHTMLEnd");
		} else {
			this.isEndOfList = false;
			rightButtonPanelHTML.setStyleName("tvSectionListGalleryRightButtonPanelHTML");
		}
	}
	

	public void setIndex(int i) {
		this.index = i;
		if (this.index == 0) {
			leftButtonPanelHTML.setStyleName("tvSectionListGalleryLeftButtonPanelHTMLEnd");
		}
	}
	
	public void setSearchBoxText(String text) {
		this.searchBox.setText(text);
	}
	
}
