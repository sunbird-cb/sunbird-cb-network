package org.sunbird.cb.hubservices.service;

import java.io.IOException;
import java.util.List;

import org.sunbird.cb.hubservices.model.ConnectionRequest;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.util.Constants;

public interface IConnectionService {

	public Response upsert(ConnectionRequest request) throws IOException;

	/**
	 * Send notification
	 *
	 * @param eventId
	 * @param userConnection
	 */
	void sendNotification(String eventId, String sender, String reciepient, String status);

	public List<String> findUserConnectionsV2(String userId, String status) throws Exception;

	public Response findSuggestedConnectionsV2(String userId, int offset, int limit);

	public Response findAllConnectionsIdsByStatusV2(String userId, String status, int offset, int limit);

	public Response findConnectionsRequestedV2(String userId, int offset, int limit, Constants.DIRECTION direction);

}
