package com.videobroadcast.server;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class BroadcastEntity {

	@Id String id;
	@Index String index = "0";
	String channelName;
	String date;
	
}
