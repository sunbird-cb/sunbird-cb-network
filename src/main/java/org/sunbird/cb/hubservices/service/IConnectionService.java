package org.sunbird.cb.hubservices.service;

import java.util.List;

import org.sunbird.cb.hubservices.model.ConnectionRequestV2;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.util.Constants;

public interface IConnectionService {

	public Response upsert(String rootOrg, ConnectionRequestV2 request) throws Exception;

	/**
	 * Send notification
	 *
	 * @param rootOrg
	 * @param eventId
	 * @param userConnection
	 */
	void sendNotification(String rootOrg, String eventId, String sender, String reciepient, String status);

	public List<String> findUserConnectionsV2(String userId, String status) throws Exception;
	public Response findSuggestedConnectionsV2(String userId, int offset, int limit);
	public Response findAllConnectionsIdsByStatusV2(String userId, String status, int offset, int limit);
	public Response findConnectionsRequestedV2(String userId, int offset, int limit, Constants.DIRECTION direction);

}
