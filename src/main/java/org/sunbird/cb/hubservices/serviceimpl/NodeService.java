package org.sunbird.cb.hubservices.serviceimpl;

import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.exception.GraphException;
import org.sunbird.cb.hubservices.exception.ValidationException;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.service.INodeService;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.hubservices.dao.IGraphDao;

import java.util.*;

@Service
public class NodeService implements INodeService {

    private Logger logger = LoggerFactory.getLogger(NodeService.class);

    @Autowired
    private IGraphDao graphDao;

    @Override
    public void connect(Node from, Node to, Map<String, String> relationProperties) {

        if (Objects.isNull(from) || Objects.isNull(to) || CollectionUtils.isEmpty(relationProperties)) {
            throw new ValidationException("Node(s) or relation properties cannot be empty");
        }

        graphDao.upsertNode(from);
        graphDao.upsertNode(to);
        graphDao.upsertRelation(from, to, relationProperties);
        logger.info("user connection successful");

    }

    @Override
    public List<Node> getNodes(String identifier, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int size, List<String> attributes) {
        checkParams(identifier, relationProperties);
        return getNodesWith(identifier, relationProperties, direction, offset, size, attributes);
    }

    @Override
    public int getNodesCount(String identifier, Map<String, String> relationProperties, Constants.DIRECTION direction) {
        int count = 0;
        if (StringUtils.isEmpty(identifier)) {
            throw new ValidationException("identifier or relation properties cannot be empty");
        }
        try {
            count = graphDao.getNeighboursCount(identifier, relationProperties, direction);

        } catch (GraphException e) {
            logger.error("Nodes count failed: {}", e);
        }
        return count;
    }


    @Override
    public List<Node> getNodeNextLevel(String identifier, Map<String, String> relationProperties, int offset, int size) {
        checkParams(identifier, relationProperties);
        return graphDao.getNeighbours(identifier, relationProperties, Constants.DIRECTION.OUT, 2,  offset, size, Arrays.asList(Constants.Graph.ID.getValue()));
    }

    private List<Node> getNodesWith(String identifier, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int size, List<String> attributes) {
        return graphDao.getNeighbours(identifier, relationProperties, direction, 1, offset, size, attributes);
    }

    private void checkParams(String identifier, Map<String, String> relationProperties) {
        if (StringUtils.isEmpty(identifier) || CollectionUtils.isEmpty(relationProperties)) {
            throw new ValidationException("identifier or relation properties cannot be empty");
        }
    }

}
