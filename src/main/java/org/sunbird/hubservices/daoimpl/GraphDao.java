package org.sunbird.hubservices.daoimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.exception.DaoLayerException;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.hubservices.dao.IGraphDao;

import javax.annotation.PostConstruct;
import java.util.*;

public class GraphDao implements IGraphDao {

    private Logger logger = LoggerFactory.getLogger(GraphDao.class);

    @Autowired
    private Driver neo4jDriver;

    private String label;

    @Autowired
    public GraphDao(String label) {
        this.label = label;
    }

    @PostConstruct
    public void init() {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        try {
            Statement statement = new Statement("CREATE CONSTRAINT ON  (n:" + label + ") ASSERT n.id IS UNIQUE");
            transaction.run(statement);
            transaction.close();
            logger.info("Added user node unique constraint successfully ");


        } catch (ClientException e) {
            transaction.rollbackAsync().toCompletableFuture();
            logger.error("Adding user node unique constraint failed : {}", e);
        }
        session.close();
    }

    @Override
    public void upsertNode(Node node) throws DaoLayerException {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        try {
            if (node != null && StringUtils.isBlank(node.getIdentifier())) {
                throw new DaoLayerException("Node identifier cannot be empty");
            }

            Map<String, Object> props = new HashMap<>();
            props = new ObjectMapper().convertValue(node, Map.class);
            props.put("id", node.getIdentifier());

            Map<String, Object> params = new HashMap<>();
            params.put("props", props);

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("CREATE (n:").append(label).append(") SET n = $props RETURN n");

            Statement statement = new Statement(queryBuilder.toString(), params);

            StatementResult result = transaction.run(statement);
            result.consume();
            transaction.commitAsync().toCompletableFuture();
            logger.info("user node with id {} created successfully ", node.getIdentifier());
            transaction.close();

        } catch (DaoLayerException e) {
            transaction.rollbackAsync().toCompletableFuture();
            logger.error("user node upsertion failed : {}", e);
            throw e;
        }
        session.close();
    }

    @Override
    public void upsertRelation(String fromUUID, String toUUID, Map<String, String> relationProperties) throws DaoLayerException {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        try {
            if (StringUtils.isBlank(fromUUID) || StringUtils.isBlank(toUUID) || CollectionUtils.isEmpty(relationProperties)) {
                throw new DaoLayerException("UUIDs or properties cannot be empty");
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("fromUUID", fromUUID);
            parameters.put("toUUID", toUUID);
            //parameters.put("propertyValue", propertyValue);
            parameters.put("props", relationProperties);

            StringBuilder query = new StringBuilder();
            query.append("MATCH (n:").append(label).append("), (n1:").append(label)
                    .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID RETURN n,n1");
            Statement statement = new Statement(query.toString(), parameters);
            StatementResult result = transaction.run(statement);
            int recordSize = result.list().size();
            result.consume();
            if (recordSize == 0) { // nodes has no relation
                throw new DaoLayerException("users with toUUID {} or fromUUID {} not found");
            } else {
                query = new StringBuilder();
                query.append("MATCH (n:").append(label).append(")-[r:connect]->(n1:").append(label)
                        .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID ").append("SET r").append(" = ")
                        .append("$props ").append("RETURN n,n1");

                statement = new Statement(query.toString(), parameters);
                result = transaction.run(statement);
                recordSize = result.list().size();
                result.consume();
                if (recordSize == 0) {

                    //TODO: use 1 query to figure out if the nodes/relation exists and then addNode & add/update the relation accordingly
                    query = new StringBuilder();
                    query.append("MATCH (n:").append(label).append("), (n1:").append(label)
                            .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID ").append("CREATE (n)-[r:connect]->(n1) ")
                            .append("SET r").append(" = ").append("$props ").append("RETURN n,n1");

                    statement = new Statement(query.toString(), parameters);
                    result = transaction.run(statement);
                    result.consume();

                }
                transaction.commitAsync().toCompletableFuture();
                logger.info("user relation with toUUID {} and fromUUID {} updated successfully ", toUUID, fromUUID);

            }
            transaction.close();

        } catch (DaoLayerException e) {
            transaction.rollbackAsync().toCompletableFuture();
            logger.error("user relation with toUUID {} and fromUUID {} updated failed : {}", toUUID, fromUUID, e);
            throw e;
        }
        session.close();
    }

    private List<Node> getNodes(List<Record> records) {
        List<Node> nodes = new ArrayList<>();
        if (records.size() > 0) {
            for (Record record : records) {
                org.neo4j.driver.v1.types.Node node = record.get("n1").asNode();
                // TODO: optimise
                Node nodePojo = new Node(node.get("identifier").asString(), node.get("name").asString(),
                        null);
                nodes.add(nodePojo);

            }
        }
        return nodes;
    }

    public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction) throws DaoLayerException {

        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();
        List<Record> records = Collections.emptyList();
        try {

            if (CollectionUtils.isEmpty(relationProperties) || StringUtils.isBlank(UUID) || Objects.isNull(direction)) {
                throw new DaoLayerException("UUID, property, direction cannot be empty");
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("UUID", UUID);
            //parameters.put("propertyValue", propertyValue);
            parameters.put("props", relationProperties);

            StringBuilder query = new StringBuilder();

            if (direction == Constants.DIRECTION.OUT) {
                query.append("MATCH (n:").append(label).append(")-[r:connect]->(n1:").append(label)
                        .append(") WHERE n.id = $UUID ");
                relationProperties.entrySet().forEach(r -> query.append(" AND r.").append(r.getKey()).append(" = ").append("'" + r.getValue() + "'"));
                query.append(" RETURN n1");

            } else {
                query.append("MATCH (n:").append(label).append(")<-[r:connect]-(n1:").append(label)
                        .append(") WHERE n.id = $UUID ");
                relationProperties.entrySet().forEach(r -> query.append(" AND r.").append(r.getKey()).append(" = ").append("'" + r.getValue() + "'"));
                query.append(" RETURN n1");
            }

            Statement statement = new Statement(query.toString(), parameters);

            StatementResult result = transaction.run(statement);
            records = result.list();
            if (records.size() == 0) {
                throw new DaoLayerException("Neighbour users for UUID " + UUID + " not found");
            }

            transaction.commitAsync().toCompletableFuture();
            logger.info("Neighbour users for UUID {} found successfully ", UUID);

        } catch (DaoLayerException e) {
            transaction.rollbackAsync().toCompletableFuture();
            logger.error("Neighbour users for UUID {} exception : {}", UUID, e);
            throw e;
        }
        session.close();
        return getNodes(records);

    }
}
