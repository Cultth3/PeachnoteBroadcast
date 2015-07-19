package com.videobroadcast.client;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;

/**
 * This class represents an explanation unit for the ExplanationGallery,
 * containing all necessary data for one explanation step
 * @author Tom
 *
 */

public class GalleryElement {

	private int elementNumber;
	private Anchor anchor;
	private Image image;
	private Image thumbnail;
	private String description;
	private String descriptionNote;
	
	public int getElementNumber() {
		return this.elementNumber;
	}
	
	public void setElementNumber(int elementNumber) {
		this.elementNumber = elementNumber;
	}
	
	public Image getImage() {
		return this.image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public Image getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(Image thumbnail) {
		this.thumbnail = thumbnail;
	}

	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescriptionNote() {
		return this.descriptionNote;
	}
	
	public void setDescriptionNote(String descriptionNote) {
		this.descriptionNote = descriptionNote;
	}
	
	public Anchor getAnchor() {
		return this.anchor;
	}
	
	public void setAnchor(Anchor anchor) {
		this.anchor = anchor;
	}

}