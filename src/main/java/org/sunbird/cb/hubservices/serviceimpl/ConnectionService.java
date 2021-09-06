package org.sunbird.cb.hubservices.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.sunbird.cb.hubservices.exception.ApplicationException;
import org.sunbird.cb.hubservices.exception.BadRequestException;
import org.sunbird.cb.hubservices.model.*;
import org.sunbird.cb.hubservices.profile.handler.IProfileRequestHandler;
import org.sunbird.cb.hubservices.profile.handler.ProfileUtils;
import org.sunbird.cb.hubservices.profile.handler.RegistryRequest;
import org.sunbird.cb.hubservices.service.IConnectionService;
import org.sunbird.cb.hubservices.service.IGraphService;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

@Service
public class ConnectionService implements IConnectionService {

	private Logger logger = LoggerFactory.getLogger(ConnectionService.class);

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ConnectionProperties connectionProperties;

	@Autowired
	IGraphService graphService;

	@Autowired
	IProfileRequestHandler profileRequestHandler;

	@Autowired
	ProfileUtils profileUtils;

	@Value("${update.profile.connections}")
	private boolean updateProfileConnections;

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

			if (created && updateProfileConnections) {
				logger.info("On add, updating connections into profile for {}", request.getUserId());
				updateProfileConnections(request.getUserId(), Constants.Status.PENDING, null, "initiatedConnections");
			}

			response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
			response.put(Constants.ResponseStatus.STATUS, HttpStatus.CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());

		}

		return response;

	}

	// @Async("connectionExecutor")
	public void updateProfileConnections(String userId, String status, Constants.DIRECTION direction, String key) {
		try {
			int count = graphService.getAllNodeCount(userId, status, direction);
			List<Node> nodes = graphService.getNodesInEdge(userId, status, 0, count);
			Map<String, Object> profileRequest = new HashMap<>();
			profileRequest.put(key, nodes);

			RegistryRequest registryRequest = profileRequestHandler.updateRequest(userId, profileRequest);
			ResponseEntity<?> responseEntity = profileUtils.getResponseEntity(ProfileUtils.URL.UPDATE.getValue(),
					registryRequest);
			logger.info("Updating profile for {}: {}", userId, responseEntity.getBody());

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Updating profile for {}: {}", userId, e);

		}

	}

	@Override
	public Response update(String rootOrg, ConnectionRequest request) throws Exception {
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

			if (updated && updateProfileConnections) {
				logger.info("On update, updating connections into profile for {}", request.getUserId());
				updateProfileConnections(request.getUserId(), request.getStatus(), Constants.DIRECTION.IN,
						request.getStatus() + "Connections");

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
	public void sendNotification(String rootOrg, String eventId, String sender, String reciepient, String status) {
		NotificationEvent event = notificationService.buildEvent(eventId, sender, reciepient, status);
		NotificationEventV2 eventV2 = notificationService.translate(event);
		notificationService.postEvent(rootOrg, eventV2);
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

			// System.out.println("commons ->"+new
			// ObjectMapper().writeValueAsString(commonConnections));
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

			List<Node> nodes = new ArrayList<>();
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
		return graphService.getAllNodes(userId).stream().map(node -> node.getIdentifier()).collect(Collectors.toList());

	}
}
