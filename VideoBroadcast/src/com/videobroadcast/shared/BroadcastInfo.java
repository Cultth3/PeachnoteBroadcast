package com.videobroadcast.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This class is an information bundle that the servlet sends to the client
 * after a broadcast was successfully created.
 * 
 * @author Tom
 *
 */

public class BroadcastInfo implements IsSerializable {

	private String broadcastId;
	private String liveStreamId;
	private String channelId;
	private String errorCode = "";
	private String broadcastTitle;
	private long liveEndTime;
	private String channelName;
	private String lifeCycleStatus;
	
	
	public String getErrorCode() {
		return this.errorCode;
	}
	
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getBroadcastId() {
		return this.broadcastId;
	}
	
	public void setBroadcastId(String broadcastId) {
		this.broadcastId = broadcastId;
	}
	
	public String getChannelId() {
		return channelId;
	}
	
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public void setLiveStreamId(String id) {
		this.liveStreamId = id;
	}

	public String getLiveStreamId() {
		return this.liveStreamId;
	}

	public String getBroadcastTitle() {
		return this.broadcastTitle;
	}
	
	public void setBroadcastTitle(String broadcastTitle) {
		this.broadcastTitle = broadcastTitle;
	}

	public long getLiveEndTime() {
		return this.liveEndTime;
	}
	
	public void setLiveEndTime(long liveEndTime) {
		this.liveEndTime = liveEndTime;
	}

	public String getChannelName() {
		return this.channelName;
	}
	
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public void setLifeCycleStatus(String lifeCycleStatus) {
		this.lifeCycleStatus = lifeCycleStatus;
	}
	
	public String getLifeCycleStatus() {
		return this.lifeCycleStatus;
	}
	
	
}
