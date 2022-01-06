package org.sunbird.cb.hubservices.serviceimpl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cb.hubservices.exception.ApplicationException;
import org.sunbird.cb.hubservices.exception.BadRequestException;
import org.sunbird.cb.hubservices.exception.ValidationException;
import org.sunbird.cb.hubservices.model.*;
import org.sunbird.cb.hubservices.service.IConnectionService;
import org.sunbird.cb.hubservices.service.INodeService;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

@Service
public class ConnectionService implements IConnectionService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ConnectionProperties connectionProperties;


    @Autowired
    INodeService nodeService;

    public Response upsert(ConnectionRequest request) throws Exception {
        Response response = new Response();
        try {

            Node from = new Node(request.getUserIdFrom());
            Node to = new Node(request.getUserIdTo());
            Map<String, String> relP = new HashMap<>();
            relP.put(Constants.Graph.STATUS.getValue(), request.getStatus());
            if (request.getCreatedAt() != null)
                relP.put(Constants.Graph.CREATED_AT.getValue(), request.getCreatedAt());
            if (request.getUpdatedAt() != null)
                relP.put(Constants.Graph.UPDATED_AT.getValue(), request.getUpdatedAt());

            nodeService.connect(from, to, relP);

            if (connectionProperties.isNotificationEnabled())
                sendNotification(connectionProperties.getNotificationTemplateRequest(), request.getUserIdFrom(),
                        request.getUserIdTo(), request.getStatus());

            response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
            response.put(Constants.ResponseStatus.STATUS, HttpStatus.CREATED);

        } catch (ValidationException ve) {
            response.put(Constants.ResponseStatus.STATUS, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());
        }

        return response;
    }

    @Override
    public void sendNotification(String eventId, String sender, String reciepient, String status) {
        NotificationEvent event = notificationService.buildEvent(eventId, sender, reciepient, status);
        notificationService.postEvent(event);
    }

    @Override
    public List<String> findUserConnectionsV2(String userId, String status) throws Exception {

        Map<String, String> relationProperties = new HashMap<>();
        relationProperties.put(Constants.Graph.STATUS.getValue(), status);
        return nodeService.getNodes(userId, relationProperties, null, 0, connectionProperties.getMaxNodeSize(), Arrays.asList(Constants.Graph.ID.getValue())).stream().map(node -> node.getId()).collect(Collectors.toList());

    }

    @Override
    public Response findSuggestedConnectionsV2(String userId, int offset, int limit) {

        Response response = new Response();
        try {
            if (userId == null || userId.isEmpty()) {
                throw new BadRequestException(Constants.Message.USER_ID_INVALID);
            }
            Map<String, String> relationProperties = new HashMap<>();
            relationProperties.put(Constants.Graph.STATUS.getValue(), Constants.Status.APPROVED);
            List<Node> nodes = nodeService.getNodeNextLevel(userId, relationProperties, offset, limit);

            List<String> allNodesIds = findUserConnectionsV2(userId, Constants.Status.APPROVED);
            List<Node> detachedNodes = nodes.stream().filter(node -> !allNodesIds.contains(node.getId()))
                    .collect(Collectors.toList());

            response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
            response.put(Constants.ResponseStatus.DATA, detachedNodes);
            response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);

        } catch (Exception e) {
            throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());

        }

        return response;
    }

    @Override
    public Response findAllConnectionsIdsByStatusV2(String userId, String status, int offset, int limit) {
        Response response = new Response();

        try {
            if (userId == null || userId.isEmpty()) {
                throw new BadRequestException(Constants.Message.USER_ID_INVALID);
            }
            Map<String, String> relationProperties = new HashMap<>();
            relationProperties.put(Constants.Graph.STATUS.getValue(), status);
            List<Node> nodes = nodeService.getNodes(userId, relationProperties, null, offset, limit, Arrays.asList(Constants.Graph.ID.getValue()));
            int count = nodeService.getNodesCount(userId, relationProperties, null);

            response.put(Constants.ResponseStatus.PAGENO, offset);
            response.put(Constants.ResponseStatus.TOTALHIT, count);

            response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
            response.put(Constants.ResponseStatus.DATA, nodes);
            response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);

        } catch (Exception e) {
            throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());
        }

        return response;
    }

    @Override
    public Response findConnectionsRequestedV2(String userId, int offset, int limit, Constants.DIRECTION direction) {
        Response response = new Response();

        try {
            if (userId == null || userId.isEmpty()) {
                throw new BadRequestException(Constants.Message.USER_ID_INVALID);
            }
            Map<String, String> relationProperties = new HashMap<>();
            relationProperties.put(Constants.Graph.STATUS.getValue(), Constants.Status.PENDING);

            List<Node> nodes = nodeService.getNodes(userId, relationProperties, direction, offset, limit, null);

            response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
            response.put(Constants.ResponseStatus.DATA, nodes);
            response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);

        } catch (Exception e) {
            throw new ApplicationException(Constants.Message.FAILED_CONNECTION + e.getMessage());
        }

        return response;
    }

}
