package org.sunbird.hubservices.dao;

import org.sunbird.cb.hubservices.model.NodeV2;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.List;
import java.util.Map;

public interface IGraphDao {

    public void upsertNode(NodeV2 node);

    public void upsertRelation(String fromUUID, String toUUID, Map<String, String> relationProperties);

    public List<NodeV2> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int limit, List<String> attributes);

    public int getNeighboursCount(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction);
}
