package org.sunbird.hubservices.dao;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.exceptions.ClientException;

import java.util.List;

public interface IGraphDao {

    public void upsertNode(String UUID) throws ClientException;

    public void upsertRelation(String fromUUID, String toUUID, String propertyName, String propertyValue) throws ClientException;

    public List<Record> getAllNeighbours(String UUID) throws ClientException;

    public List<Record> getNeighboursByRelation(String UUID, String propertyName, String propertyValue) throws ClientException;


}
