package org.sunbird.cb.hubservices.service;

import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.List;
import java.util.Map;

public interface INodeService {

    public Boolean connect(Node from, Node to, Map<String, String> relationProperties);

    public List<Node> getNodeByOutRelation(String identifier, Map<String, String> relationProperties, int offset, int size) throws Exception;

    public List<Node> getNodeByInRelation(String identifier, Map<String, String> relationProperties, int offset, int size) throws Exception;

    public List<Node> getNodeNextLevel(String identifier, Map<String, String> relationProperties, int offset, int size) throws Exception;

}
