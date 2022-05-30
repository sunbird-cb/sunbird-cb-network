package org.sunbird.cb.hubservices.service;

import java.util.List;
import java.util.Map;

import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;

public interface IGraphService {

	public Boolean createNodeWithRelation(Node from, Node to, String relation) throws Exception;

	public Boolean deleteRelation(Node from, Node to, String relation) throws Exception;

	public List<Node> getNodesInEdge(String identifier, String relation, int offset, int size) throws Exception;

	public List<Node> getNodesOutEdge(String identifier, String relation, int offset, int size) throws Exception;

	public List<Node> getNodesInAndOutEdge(String identifier, String relation, int offset, int size) throws Exception;

	public List<Node> getNodesNextLevel(String identifier, String relation, int offset, int size) throws Exception;

	public int getAllNodeCount(String identifier, String relation, Constants.DIRECTION direction) throws Exception;

	public List<Node> getAllNodes(String identifier) throws Exception;


	public List<Node> getNodes(String identifier, Map<String, String> relationProperties, Constants.DIRECTION direction,
							   int offset, int size, List<String> attributes);

}
