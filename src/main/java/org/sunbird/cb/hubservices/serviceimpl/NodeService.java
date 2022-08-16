package org.sunbird.cb.hubservices.serviceimpl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class NodeService implements INodeService {

	private Logger logger = LoggerFactory.getLogger(NodeService.class);

	@Autowired
	private IGraphDao graphDao;

	@Override
	public Boolean connect(Node from, Node to, Map<String, String> relationProperties) throws Exception {

		if (!(Objects.isNull(from) || Objects.isNull(to) || CollectionUtils.isEmpty(relationProperties) || from.getId().equalsIgnoreCase(to.getId())))
		{
			Boolean isNodeFromPresent = graphDao.upsertNode(from);
			Boolean isNodeToPresent = graphDao.upsertNode(to);
			if(isNodeToPresent && isNodeFromPresent) {
				return graphDao.upsertRelation(from, to, relationProperties);
			}
		}
		return  Boolean.FALSE;
	}

	@Override
	public List<Node> getNodes(String id, Map<String, String> relationProperties, Constants.DIRECTION direction,
							   int offset, int size, List<String> attributes) {
		checkParams(id, relationProperties);
		return getNodesWith(id, relationProperties, direction, offset, size, attributes);
	}

	@Override
	public int getNodesCount(String id, Map<String, String> relationProperties, Constants.DIRECTION direction) {
		int count = 0;
		if (StringUtils.isEmpty(id)) {
			throw new ValidationException("id or relation properties cannot be empty");
		}
		try {
			count = graphDao.getNeighboursCount(id, relationProperties, direction);

		} catch (GraphException e) {
			logger.error("Nodes count failed: {}", e);
		}
		return count;
	}

	@Override
	public List<Node> getNodeNextLevel(String id, Map<String, String> relationProperties, int offset,
									   int size) {
		checkParams(id, relationProperties);
		return graphDao.getNeighbours(id, relationProperties, Constants.DIRECTION.OUT, 2, offset, size,
				Arrays.asList(Constants.Graph.ID.getValue()));
	}

	private List<Node> getNodesWith(String id, Map<String, String> relationProperties,
									Constants.DIRECTION direction, int offset, int size, List<String> attributes) {
		return graphDao.getNeighbours(id, relationProperties, direction, 1, offset, size, attributes);
	}

	private void checkParams(String id, Map<String, String> relationProperties) {
		if (StringUtils.isEmpty(id) || CollectionUtils.isEmpty(relationProperties)) {
			throw new ValidationException("id or relation properties cannot be empty");
		}
	}

}