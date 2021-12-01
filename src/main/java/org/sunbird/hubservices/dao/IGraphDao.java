package org.sunbird.hubservices.dao;

import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.List;
import java.util.Map;

public interface IGraphDao {

    public void upsertNode(Node node);

    public void upsertRelation(Node nodeFrom, Node nodeTo, Map<String, String> relationProperties);

    public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int limit, List<String> attributes);

    public int getNeighboursCount(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction);
}
