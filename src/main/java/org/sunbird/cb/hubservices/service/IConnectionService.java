package org.sunbird.cb.hubservices.service;

import org.sunbird.cb.hubservices.model.ConnectionRequest;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.List;
import java.util.Map;

public interface IConnectionService {

	/**
     * Method to upsert the nodes
     *
     * @param from
     * @param to
     * @param relP
     * @param updateOperation
     * @return
     */
	Response upsert(ConnectionRequest request, String updateOperation);

	/**
	 * Validate if a user already exists or if the request params are correct
	 * @param request
	 * @return
	 */
	boolean validateRequest(ConnectionRequest request);


	/**
	 * Send Notification
	 * @param eventId
	 * @param sender
	 * @param reciepient
	 * @param status
	 */
	void sendNotification(String eventId, String sender, String reciepient, String status);

	/**
	 * Find all userids which are connected or Approved to this userId/NodeId
	 * @param userId
	 * @param status
	 * @return
	 * @throws Exception
	 */
	public List<String> findUserConnectionsV2(String userId, String status) throws Exception;

	/**
	 * Find all the suggested connections for this user
	 * @param userId
	 * @param offset
	 * @param limit
	 * @return
	 */
	public Response findSuggestedConnectionsV2(String userId, int offset, int limit);

	/**
	 * Find all connection ids according to the status
	 * @param userId
	 * @param status
	 * @param offset
	 * @param limit
	 * @return
	 */
	public Response findAllConnectionsIdsByStatusV2(String userId, String status, int offset, int limit);

	/**
	 * Find all connections requested
	 * @param userId
	 * @param offset
	 * @param limit
	 * @param direction
	 * @return
	 */
	public Response findConnectionsRequestedV2(String userId, int offset, int limit, Constants.DIRECTION direction);

	/**
	 * Method to set relationship properties
	 * @param request
	 * @param from
	 * @param to
	 * @return
	 */
	Map<String, String> setRelationshipProperties(ConnectionRequest request, Node from, Node to);

}
