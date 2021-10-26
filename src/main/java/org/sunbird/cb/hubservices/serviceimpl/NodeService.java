package org.sunbird.cb.hubservices.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.exception.DaoLayerException;
import org.sunbird.cb.hubservices.exception.NetworkClientException;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.service.INodeService;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.hubservices.dao.IGraphDao;

import java.util.*;

@Service
public class NodeService implements INodeService {

    private Logger logger = LoggerFactory.getLogger(NodeService.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IGraphDao graphDao;

    @Override
    public Boolean connect(Node from, Node to, Map<String, String> relationProperties) {

        Boolean flag = Boolean.FALSE;
        if (Objects.isNull(from) || Objects.isNull(to) || CollectionUtils.isEmpty(relationProperties)) {
            throw new NetworkClientException("Node(s) or relation properties cannot be empty");
        }
        try {
            graphDao.upsertNode(from);
            graphDao.upsertNode(to);
            graphDao.upsertRelation(from.getIdentifier(), to.getIdentifier(), relationProperties);
            flag = Boolean.TRUE;
            logger.info("user connection successful");

        } catch (DaoLayerException d) {
            logger.error("user connection failed : {}", d);

        }
        return flag;
    }

    @Override
    public List<Node> getNodeByOutRelation(String identifier, Map<String, String> relationProperties, int offset, int size) {

        checkParams(identifier, relationProperties);

        return getNodesWith(identifier, relationProperties, Constants.DIRECTION.OUT, offset, size);
    }

    @Override
    public List<Node> getNodeByInRelation(String identifier, Map<String, String> relationProperties, int offset, int size) {

        checkParams(identifier, relationProperties);

        return getNodesWith(identifier, relationProperties, Constants.DIRECTION.IN, offset, size);
    }


    @Override
    public List<Node> getNodeNextLevel(String identifier, Map<String, String> relationProperties, int offset, int size) {

        checkParams(identifier, relationProperties);

        List<Node> nodes = new ArrayList<>();
        for (Node n : getNodesWith(identifier, relationProperties, Constants.DIRECTION.OUT, offset, size)) {
            nodes.addAll(getNodesWith(n.getIdentifier(), relationProperties, Constants.DIRECTION.OUT, offset, size));
        }
        return nodes;
    }

    private List<Node> getNodesWith(String identifier, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int size) {
        List<Node> nodes = Collections.emptyList();
        try {
            nodes = graphDao.getNeighbours(identifier, relationProperties, direction, offset, size);
        } catch (DaoLayerException d) {
            logger.error(" Fetching user nodes for relations failed : {}", d);
        }
        return nodes;


    }

    private void checkParams(String identifier, Map<String, String> relationProperties) {
        if (StringUtils.isEmpty(identifier) || CollectionUtils.isEmpty(relationProperties)) {
            throw new NetworkClientException("identifier or relation properties cannot be empty");
        }
    }

}
