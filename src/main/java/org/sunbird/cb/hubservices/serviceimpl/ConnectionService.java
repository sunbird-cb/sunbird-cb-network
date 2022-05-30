package org.sunbird.cb.hubservices.serviceimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cb.hubservices.exception.ApplicationException;
import org.sunbird.cb.hubservices.exception.BadRequestException;
import org.sunbird.cb.hubservices.model.ConnectionRequest;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.model.NotificationEvent;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.service.IConnectionService;
import org.sunbird.cb.hubservices.service.IGraphService;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectionService implements IConnectionService {

	private final Logger logger = LoggerFactory.getLogger(ConnectionService.class);

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ConnectionProperties connectionProperties;

	@Autowired
	IGraphService graphService;

	@Override
	public Response add(String rootOrg, ConnectionRequest request) throws Exception {

		Response response = new Response();
		try {

			Node from = new Node(request.getUserId(), request.getUserName(), request.getUserDepartment());
			// from.setCreatedAt(new Date());
			from.setUpdatedAt(new Date());

			Node to = new Node(request.getConnectionId(), request.getConnectionName(),
					request.getConnectionDepartment());
			to.setCreatedAt(new Date());
			// to.setUpdatedAt(to.getCreatedAt());

			boolean created = graphService.createNodeWithRelation(from, to, Constants.Status.PENDING);
			if (connectionProperties.isNotificationEnabled() && created)
				sendNotification(rootOrg, connectionProperties.getNotificationTemplateRequest(), request.getUserId(),
						request.getConnectionId(), Constants.Status.PENDING);

			if (created && connectionProperties.isUpdateProfileConnections()) {
				logger.info("On add, updating connections into profile for {}", request.getUserId());
			}

			response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
			response.put(Constants.ResponseStatus.STATUS, HttpStatus.CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());

		}

		return response;

	}

	@Override
	public Response update(String rootOrg, ConnectionRequest request) {
		Response response = new Response();

		try {

			Node from = new Node(request.getUserId(), request.getUserName(), request.getUserDepartment());
			from.setUpdatedAt(new Date());
			Node to = new Node(request.getConnectionId(), request.getConnectionName(),
					request.getConnectionDepartment());
			to.setUpdatedAt(new Date());

			graphService.deleteRelation(from, to, null);
			Boolean updated = graphService.createNodeWithRelation(to, from, request.getStatus());

			if (connectionProperties.isNotificationEnabled() && updated)
				sendNotification(rootOrg, connectionProperties.getNotificationTemplateResponse(),
						request.getConnectionId(), request.getUserId(), request.getStatus());

			if (updated && connectionProperties.isUpdateProfileConnections()) {
				logger.info("On update, updating connections into profile for {}", request.getUserId());

			}
			response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
			response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());

		}

		return response;
	}

	@Override
	public void sendNotification(String rootOrg, String eventId, String sender, String recipient, String status) {
		NotificationEvent event = notificationService.buildEvent(eventId, sender, recipient, status);
		notificationService.postEvent(rootOrg, event);
	}

	@Override
	public Response findSuggestedConnections(String rootOrg, String userId, int offset, int limit) {

		Response response = new Response();
		try {
			if (userId == null || userId.isEmpty()) {
				throw new BadRequestException(Constants.Message.USER_ID_INVALID);
			}
			List<Node> nodes = graphService.getNodesNextLevel(userId, Constants.Status.APPROVED, offset, limit);

			List<String> allNodesIds = findUserConnections(rootOrg, userId);
			List<Node> detachedNodes = nodes.stream().filter(node -> !allNodesIds.contains(node.getIdentifier()))
					.collect(Collectors.toList());

			if (detachedNodes.isEmpty()) {
				response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.FAILED);
				response.put(Constants.ResponseStatus.DATA, detachedNodes);
				response.put(Constants.ResponseStatus.STATUS, HttpStatus.NO_CONTENT);
			}
			response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
			response.put(Constants.ResponseStatus.DATA, detachedNodes);
			response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);

		} catch (Exception e) {
			throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());

		}

		return response;
	}

	@Override
	public Response findAllConnectionsIdsByStatus(String rootOrg, String userId, String status, int offset, int limit) {
		Response response = new Response();

		try {
			if (userId == null || userId.isEmpty()) {
				throw new BadRequestException(Constants.Message.USER_ID_INVALID);
			}

			List<Node> nodes = graphService.getNodesInAndOutEdge(userId, status, offset, limit);
			int count = graphService.getAllNodeCount(userId, status, null);

			response.put(Constants.ResponseStatus.PAGENO, offset);
			// response.put(Constants.ResponseStatus.HASPAGENEXT,
			// sliceUserConnections.hasNext());
			response.put(Constants.ResponseStatus.TOTALHIT, count);

			if (nodes.isEmpty()) {
				response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.FAILED);
				response.put(Constants.ResponseStatus.DATA, nodes);
				response.put(Constants.ResponseStatus.STATUS, HttpStatus.NO_CONTENT);
			}
			response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
			response.put(Constants.ResponseStatus.DATA, nodes);
			response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);

		} catch (Exception e) {
			throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());
		}

		return response;
	}

	@Override
	public Response findConnectionsRequested(String rootOrg, String userId, int offset, int limit,
			Constants.DIRECTION direction) {
		Response response = new Response();

		try {
			if (userId == null || userId.isEmpty()) {
				throw new BadRequestException(Constants.Message.USER_ID_INVALID);
			}

			List<Node> nodes;
			if (direction.equals(Constants.DIRECTION.IN))
				nodes = graphService.getNodesInEdge(userId, Constants.Status.PENDING, offset, limit);

			else
				nodes = graphService.getNodesOutEdge(userId, Constants.Status.PENDING, offset, limit);

			if (nodes.isEmpty()) {
				response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.FAILED);
				response.put(Constants.ResponseStatus.DATA, nodes);
				response.put(Constants.ResponseStatus.STATUS, HttpStatus.NO_CONTENT);
			}
			response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
			response.put(Constants.ResponseStatus.DATA, nodes);
			response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);

		} catch (Exception e) {
			throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());
		}

		return response;
	}

	@Override
	public List<String> findUserConnections(String rootOrg, String userId) throws Exception {
		return graphService.getAllNodes(userId).stream().map(Node::getIdentifier).collect(Collectors.toList());
	}

	@Override
	public List<String> findUserConnectionsV2(String userId, String status)  {
		Map<String, String> relationProperties = new HashMap<>();
		relationProperties.put(Constants.Graph.STATUS.getValue(), status);
		return graphService
				.getNodes(userId, relationProperties, null, 0, 300,
						Collections.singletonList(Constants.Graph.IDENTIFIER.getValue()))
				.stream().map(Node::getIdentifier).collect(Collectors.toList());

	}
}
