package com.videobroadcast.client;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Element for the progress bar over the explanation gallery in the Go Live Section
 * @author Tom
 *
 */

public class ProgressBarElement extends Composite {
	
	private VerticalPanel mainPanel = new VerticalPanel();
	
	private HorizontalPanel textPanel = new HorizontalPanel();
	private Label stepText; 
	private Image mutedImage;
	private Image completedImage;
	private Image greenHook;
	
	private boolean isDone = false;

	private HTML imageWrapper;
	
	public ProgressBarElement(View view, String stepText, Image mutedImage, Image completedImage) {
		initWidget(this.mainPanel);
		
		this.setStyleName("ProgressBarElement");
		this.getElement().getStyle().setCursor(Cursor.POINTER);
	
//		this.greenHook = new Image(Resources.INSTANCE.greenHook().getSafeUri());
		this.greenHook = new Image("http://i.imgur.com/AqtAojD.png");
		this.greenHook.setPixelSize(24, 22);
		this.greenHook.setStyleName("greenHook");
		this.stepText = new Label(stepText);
		this.textPanel.add(this.stepText);
		this.stepText.setStyleName("ProgressBarElementStepText");
		this.textPanel.setStyleName("ProgressBarElementStepTextPanel");
		this.mutedImage = mutedImage;
		this.completedImage = completedImage;
		
		imageWrapper = new HTML();
		imageWrapper.setStyleName("progressBarElementImageWrapper");
		imageWrapper.setHTML("<div style=\"width: 84px; height: 84px; overflow: hidden;\"> <img style=\"margin-top: " 
		+ -20 + "px; margin-left: -11px; width: 106px; height: 122px;\" src=\"" + this.mutedImage.getUrl() + "\">");
//		this.anchor.getElement().getStyle().setCursor(Cursor.POINTER);
//		this.anchor.getElement().appendChild(imageWrapper.getElement());
		
		this.mainPanel.add(this.textPanel);
		this.mainPanel.add(imageWrapper);
		
	}
	
	// Make the ProgressBarElement current
	public void setMarked(boolean marked) {
		if (marked) {
			this.setStyleName("ProgressBarElementMarked");
		} else {
			if (isDone) // showCompletedImage
				this.setStyleName("ProgressBarElementDone");
			else
				this.setStyleName("ProgressBarElement");
		}
	}
	
	public void setDone(boolean completed) {
		if (!isDone && completed) {
			isDone = true;
//			this.anchor.getElement().removeAllChildren();
//			this.anchor.getElement().appendChild(this.completedImage.getElement());
			this.imageWrapper.setHTML("<div style=\"width: 84px; height: 84px; overflow: hidden;\"> <img style=\"margin-top: " 
					+ -20 + "px; margin-left: -11px; width: 106px; height: 122px;\" src=\"" + this.completedImage.getUrl() + "\">");
			this.setStyleName("ProgressBarElementDone");
			this.stepText.setStyleName("ProgressBarElementDoneStepText");
			this.textPanel.setStyleName("ProgressBarElementDoneStepTextPanel");
			this.textPanel.add(this.greenHook);		
					
		} else if (isDone && completed == false) {
			isDone = false;
//			this.anchor.getElement().removeAllChildren();
//			this.anchor.getElement().appendChild(this.mutedImage.getElement());
			imageWrapper.setHTML("<div style=\"width: 84px; height: 84px; overflow: hidden;\"> <img style=\"margin-top: " 
					+ -20 + "px; margin-left: -11px; width: 106px; height: 122px;\" src=\"" + this.mutedImage.getUrl() + "\">");
			this.setStyleName("ProgressBarElement");
			this.stepText.setStyleName("ProgressBarElementStepText");
			this.textPanel.setStyleName("ProgressBarElementStepTextPanel");
			this.textPanel.remove(this.greenHook);
		}
	}
	
	public boolean isDone() {
		return this.isDone;
	}
	
}
