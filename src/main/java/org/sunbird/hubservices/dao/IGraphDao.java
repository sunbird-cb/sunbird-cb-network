package org.sunbird.hubservices.dao;

import java.util.List;
import java.util.Map;

import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;

public interface IGraphDao {

	public Boolean upsertNode(Node node) throws Exception ;

	public Boolean upsertRelation(Node nodeFrom, Node nodeTo, Map<String, String> relationProperties) throws Exception;

	public int getNeighboursCount(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction);

	public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction,
			int level, int offset, int limit, List<String> attributes);
}
