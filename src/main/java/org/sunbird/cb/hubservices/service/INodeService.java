package org.sunbird.cb.hubservices.service;

import java.util.List;
import java.util.Map;

import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;

public interface INodeService {

	public Boolean connect(Node from, Node to, Map<String, String> relationProperties) throws Exception;

	public List<Node> getNodeNextLevel(String id, Map<String, String> relationProperties, int offset, int size);

	public int getNodesCount(String id, Map<String, String> relationProperties, Constants.DIRECTION direction);

	public List<Node> getNodes(String id, Map<String, String> relationProperties, Constants.DIRECTION direction,
			int offset, int size, List<String> attributes);

}
