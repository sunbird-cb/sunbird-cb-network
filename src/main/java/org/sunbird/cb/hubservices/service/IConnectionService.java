package org.sunbird.cb.hubservices.service;

import java.util.List;

import org.sunbird.cb.hubservices.model.ConnectionRequest;
import org.sunbird.cb.hubservices.model.ConnectionRequestV2;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.util.Constants;

public interface IConnectionService {

	/**
	 * Creates a connection
	 * 
	 * @param rootOrg
	 * @param request
	 * @return
	 */
	Response add(String rootOrg, ConnectionRequest request) throws Exception;

	public Response upsert(String rootOrg, ConnectionRequestV2 request) throws Exception;

	/**
	 * To update the status and dates of connection
	 * 
	 * @param rootOrg
	 * @param request
	 * @return
	 */
	Response update(String rootOrg, ConnectionRequest request) throws Exception;

	/**
	 * Find related connections from existing connections
	 * 
	 * @param userId
	 * @param offset
	 * @param limit
	 * @return
	 */
	Response findSuggestedConnections(String rootOrg, String userId, int offset, int limit);

	public Response findAllConnectionsIdsByStatus(String rootOrg, String userId, String status, int offset, int limit);

	/**
	 * Find connections which is not established/pending for approval
	 * 
	 * @param userId
	 * @return
	 */
	Response findConnectionsRequested(String rootOrg, String userId, int offset, int limit,
			Constants.DIRECTION direction);

	/**
	 * Send notification
	 *
	 * @param rootOrg
	 * @param eventId
	 * @param userConnection
	 */
	void sendNotification(String rootOrg, String eventId, String sender, String reciepient, String status);

	List<String> findUserConnections(String rootOrg, String userId) throws Exception;

	public List<String> findUserConnectionsV2(String userId, String status) throws Exception;
	public Response findSuggestedConnectionsV2(String userId, int offset, int limit);
	public Response findAllConnectionsIdsByStatusV2(String userId, String status, int offset, int limit);
	public Response findConnectionsRequestedV2(String userId, int offset, int limit, Constants.DIRECTION direction);

}
