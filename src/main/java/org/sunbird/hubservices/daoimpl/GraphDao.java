package org.sunbird.hubservices.daoimpl;

import io.micrometer.core.instrument.util.StringUtils;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.sunbird.hubservices.dao.IGraphDao;

import java.util.*;

public class GraphDao implements IGraphDao {

    private Logger logger = LoggerFactory.getLogger(GraphDao.class);

    @Autowired
    private Driver neo4jDriver;

    private String nodeType;

    @Autowired
    public GraphDao(String nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public void upsertNode(String UUID) throws ClientException {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        try {
            if (StringUtils.isBlank(UUID)) {
                throw new ClientException("UUID cannot be empty");
            }

            Map<String, Object> props = new HashMap<>();
            props.put("id", UUID);
            //props.put( "name", "tag-"+UUID );

            Map<String, Object> params = new HashMap<>();
            params.put("props", props);
            params.put("uuid", UUID);

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (n:").append(nodeType).append(") WHERE n.id = $uuid RETURN n");
            Statement statement = new Statement(queryBuilder.toString(), params);

            StatementResult result = transaction.run(statement);
            int recordSize = result.list().size();
            logger.info("records {} ", recordSize);
            result.consume();

            if (recordSize == 0) {
                queryBuilder = new StringBuilder();
                queryBuilder.append("CREATE (n:").append(nodeType).append(") SET n = $props RETURN n");
                statement = new Statement(queryBuilder.toString(), params);
                result = transaction.run(statement);
                result.consume();
                transaction.commitAsync().toCompletableFuture();
                logger.info("user node with id {} created successfully ", UUID);
            } else {
                logger.info("user node with id {} exist ", UUID);

            }
            transaction.close();

        } catch (ClientException e) {
            transaction.rollbackAsync().toCompletableFuture();
            logger.error("user node upsertion failed : {}", e);
        }
        session.close();
    }

    @Override
    public void upsertRelation(String fromUUID, String toUUID, String propertyName, String propertyValue) throws ClientException {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        try {
            if (StringUtils.isBlank(fromUUID) || StringUtils.isBlank(toUUID) || StringUtils.isBlank(propertyName) || StringUtils.isBlank(propertyValue)) {
                throw new ClientException("UUIDs or property cannot be empty");
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("fromUUID", fromUUID);
            parameters.put("toUUID", toUUID);
            parameters.put("propertyValue", propertyValue);

            StringBuilder query = new StringBuilder();
            query.append("MATCH (n:").append(nodeType).append("), (n1:").append(nodeType)
                    .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID RETURN n,n1");
            Statement statement = new Statement(query.toString(), parameters);
            StatementResult result = transaction.run(statement);
            int recordSize = result.list().size();
            result.consume();
            if (recordSize == 0) { // nodes has no relation
                throw new NoSuchRecordException("users with toUUID {} or fromUUID {} not found");
            } else {
                query = new StringBuilder();
                query.append("MATCH (n:").append(nodeType).append(")-[r:connect]->(n1:").append(nodeType)
                        .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID ").append("SET r.").append(propertyName)
                        .append(" = $propertyValue ").append("RETURN n,n1");

                statement = new Statement(query.toString(), parameters);
                result = transaction.run(statement);
                recordSize = result.list().size();
                result.consume();
                if (recordSize == 0) {

                    query = new StringBuilder();
                    query.append("MATCH (n:").append(nodeType).append("), (n1:").append(nodeType)
                            .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID ").append("CREATE (n)-[r:connect]->(n1) ")
                            .append("SET r.").append(propertyName).append(" = $propertyValue ").append("RETURN n,n1");

                    statement = new Statement(query.toString(), parameters);
                    result = transaction.run(statement);
                    result.consume();

                }
                transaction.commitAsync().toCompletableFuture();
                logger.info("user relation with toUUID {} and fromUUID {} updated successfully ", toUUID, fromUUID);

            }
            transaction.close();

        } catch (ClientException e) {
            transaction.rollbackAsync().toCompletableFuture();
            logger.error("user relation with toUUID {} and fromUUID {} updated failed : {}", toUUID, fromUUID, e);
        }
        session.close();
    }


    @Override
    public List<Record> getAllNeighbours(String UUID) throws ClientException {

        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();
        List<Record> records = Collections.emptyList();

        try {

            if (StringUtils.isBlank(UUID)) {
                throw new ClientException("UUID cannot be empty");
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("UUID", UUID);

            StringBuilder query = new StringBuilder();
            query.append("MATCH (n:").append(nodeType).append(")-[r:connect]->(n1:").append(nodeType)
                    .append(") WHERE n.id = $UUID RETURN n1");

            Statement statement = new Statement(query.toString(), parameters);

            StatementResult result = transaction.run(statement);
            records = result.list();
            if (records.size() == 0) {
                throw new NoSuchRecordException("Neighbour users for UUID {} not found");
            }

            transaction.commitAsync().toCompletableFuture();
            logger.info("Neighbour users for UUID {} found  ", UUID);

        } catch (ClientException e) {
            transaction.rollbackAsync().toCompletableFuture();
            logger.error("Neighbour users for UUID {} could not be found with exception : {}", UUID, e);
        }
        session.close();
        return records;
    }

    @Override
    public List<Record> getNeighboursByRelation(String UUID, String propertyName, String propertyValue) throws ClientException {

        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();
        List<Record> records = Collections.emptyList();

        try {

            if (StringUtils.isBlank(UUID) || StringUtils.isBlank(propertyName) || StringUtils.isBlank(propertyValue)) {
                throw new ClientException("UUID, property cannot be empty");
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("UUID", UUID);
            parameters.put("propertyValue", propertyValue);

            StringBuilder query = new StringBuilder();
            query.append("MATCH (n:").append(nodeType).append(")-[r:connect]->(n1:").append(nodeType)
                    .append(") WHERE n.id = $UUID AND r.").append(propertyName).append(" = $propertyValue RETURN n1");

            Statement statement = new Statement(query.toString(), parameters);

            StatementResult result = transaction.run(statement);
            records = result.list();
            if (records.size() == 0) {
                throw new NoSuchRecordException("Neighbour users for UUID {} not found");
            }

            transaction.commitAsync().toCompletableFuture();
            logger.info("Neighbour users for UUID {} found successfully ", UUID);

        } catch (ClientException e) {
            transaction.rollbackAsync().toCompletableFuture();
            logger.error("Neighbour users for UUID {} exception : {}", UUID, e);
        }
        session.close();
        return records;

    }
}
