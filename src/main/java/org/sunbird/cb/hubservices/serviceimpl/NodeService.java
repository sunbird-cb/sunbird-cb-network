package org.sunbird.cb.hubservices.serviceimpl;

import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.exception.ErrorCode;
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
    public Boolean connect(Node from, Node to, Map<String, String> relationProperties) {

        Boolean flag = Boolean.FALSE;
        if (Objects.isNull(from) || Objects.isNull(to) || CollectionUtils.isEmpty(relationProperties)) {
            throw new ValidationException("Node(s) or relation properties cannot be empty");
        }
        try {
            graphDao.upsertNode(from);
            graphDao.upsertNode(to);
            graphDao.upsertRelation(from, to, relationProperties);
            flag = Boolean.TRUE;
            logger.info("user connection successful");

        } catch (GraphException d) {
            logger.error("node connection failed : {}", d);

        }
        return flag;
    }

    @Override
    public List<Node> getNodeByOutRelation(String identifier, Map<String, String> relationProperties, int offset, int size) {

        checkParams(identifier, relationProperties);

        return getNodesWith(identifier, relationProperties, Constants.DIRECTION.OUT, offset, size, null);
    }

    @Override
    public List<Node> getNodeByInRelation(String identifier, Map<String, String> relationProperties, int offset, int size) {

        checkParams(identifier, relationProperties);

        return getNodesWith(identifier, relationProperties, Constants.DIRECTION.IN, offset, size, null);
    }

    @Override
    public List<Node> getAllNodes(String identifier, Map<String, String> relationProperties, int offset, int size) {

        checkParams(identifier, relationProperties);

        return getNodesWith(identifier, relationProperties, null, offset, size, Arrays.asList(Constants.Graph.ID.getValue()));
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

        List<Node> nodes = new ArrayList<>();
        for (Node n : getNodesWith(identifier, relationProperties, Constants.DIRECTION.OUT, offset, size,null)) {
            nodes.addAll(getNodesWith(n.getId(), relationProperties, Constants.DIRECTION.OUT, offset, size, null));
        }
        return nodes;
    }

    private List<Node> getNodesWith(String identifier, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int size, List<String> attr) {
        return graphDao.getNeighbours(identifier, relationProperties, direction, offset, size, attr);
    }

    private void checkParams(String identifier, Map<String, String> relationProperties) {
        if (StringUtils.isEmpty(identifier) || CollectionUtils.isEmpty(relationProperties)) {
            throw new ValidationException("identifier or relation properties cannot be empty");
        }
    }

}
