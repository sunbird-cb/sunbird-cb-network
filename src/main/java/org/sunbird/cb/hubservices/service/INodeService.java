package org.sunbird.cb.hubservices.service;

import org.sunbird.cb.hubservices.model.NodeV2;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.List;
import java.util.Map;

public interface INodeService {

    public Boolean connect(NodeV2 from, NodeV2 to, Map<String, String> relationProperties);

    public List<NodeV2> getNodeByOutRelation(String identifier, Map<String, String> relationProperties, int offset, int size);

    public List<NodeV2> getNodeByInRelation(String identifier, Map<String, String> relationProperties, int offset, int size);

    public List<NodeV2> getAllNodes(String identifier, Map<String, String> relationProperties, int offset, int size);

    public List<NodeV2> getNodeNextLevel(String identifier, Map<String, String> relationProperties, int offset, int size);

    public int getNodesCount(String identifier, Map<String, String> relationProperties, Constants.DIRECTION direction);
}
