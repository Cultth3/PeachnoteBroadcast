package com.videobroadcast.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/*
 * Just a help class to return a tuple of the broadcast-id and channelName 
 * for the RPC call in the clientService's method loadBroadcastListFromDatastore()
 */

public class IdNameTuple implements IsSerializable {

	public String id;
	public String channelName;
	
	public IdNameTuple() {
	}
	
	public IdNameTuple(String id, String channelName) {
		this.id = id;
		this.channelName = channelName;
	}
	
}
