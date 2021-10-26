package org.sunbird.hubservices.dao;

import org.sunbird.cb.hubservices.exception.DaoLayerException;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.List;
import java.util.Map;

public interface IGraphDao {

    public void upsertNode(Node node) throws DaoLayerException;

    public void upsertRelation(String fromUUID, String toUUID, Map<String, String> relationProperties) throws DaoLayerException;

    public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int limit) throws DaoLayerException;

}
