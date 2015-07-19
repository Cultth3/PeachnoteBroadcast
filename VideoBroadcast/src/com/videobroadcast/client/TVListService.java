package com.videobroadcast.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.videobroadcast.shared.BroadcastInfo;

@RemoteServiceRelativePath("videobroadcast/tvsectionvideolistservletremote")
public interface TVListService extends RemoteService {

	List<BroadcastInfo> giveVideoList(String searchTerm);

	String deleteVideo(String token, String broadcastId);

}
