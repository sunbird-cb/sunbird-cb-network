package org.sunbird.hubservices.daoimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.SessionExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.exception.ErrorCode;
import org.sunbird.cb.hubservices.exception.GraphException;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.hubservices.dao.IGraphDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.neo4j.driver.internal.types.InternalTypeSystem.TYPE_SYSTEM;
import static org.neo4j.driver.v1.Values.parameters;

public class GraphDao implements IGraphDao {

    private final Logger logger = LoggerFactory.getLogger(GraphDao.class);

    @Autowired
    private Driver neo4jDriver;

    private final String label;

    @Autowired
    public GraphDao(String label) {
        this.label = label;
    }

    @Override
    public Boolean upsertNode(Node node) throws Exception {
        try (Session session = neo4jDriver.session();Transaction transaction = session.beginTransaction()) {
            Statement statement = new Statement("MATCH (n:" + label + ") WHERE n.id=$fromUUID " + "RETURN n", parameters(Constants.FROM_UUID, node.getId()));
            StatementResult result = transaction.run(statement);
            List<Record> existingNodes = result.list();
            result.consume();
            if (!existingNodes.isEmpty()) {
                logger.info("Nodes exists, new node cannot be created! ");
            } else {
                logger.info("Node doesn't exists, new node can be created! ");
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.Graph.PROPS.getValue(), new ObjectMapper().convertValue(node, Map.class));
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("CREATE (n:").append(label).append(") SET n = $props RETURN n");
                statement = new Statement(queryBuilder.toString(), params);
                result = transaction.run(statement);
                result.consume();
                transaction.commitAsync().toCompletableFuture().get();
                logger.info("user node with id {} created successfully ", node.getId());
            }
        } catch (Exception e) {
            logger.error("user node creation failed : ", e);
            return Boolean.FALSE;

        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean upsertRelation(Node nodeFrom, Node nodeTo, Map<String, String> relationProperties) throws Exception {
        boolean isUpserted = Boolean.FALSE;
        try (Session session = neo4jDriver.session(); Transaction transaction = session.beginTransaction()) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(Constants.FROM_UUID, nodeFrom.getId());
            parameters.put(Constants.TO_UUID, nodeTo.getId());
            parameters.put(Constants.Graph.PROPS.getValue(), relationProperties);

            String queryNodeExistWithReverseEdge = "MATCH (n:" + label + ")<-[r:connect]-(n1:" +
                    label + ") WHERE n.id = $fromUUID AND n1.id = $toUUID " + "RETURN n,n1";

            Statement statement = new Statement(queryNodeExistWithReverseEdge, parameters);
            StatementResult result = transaction.run(statement);
            int recordSize = result.list().size();
            result.consume();
            if (recordSize != 0) {
                if (logger.isDebugEnabled())
                    logger.debug("updating user relation with fromUUID {} and toUUID {} ", nodeFrom.getId(), nodeTo.getId());
                isUpserted = updateRelationshipBetweenTwoNodes(nodeFrom, nodeTo, statement, result, transaction, recordSize, relationProperties);
                transaction.commitAsync().toCompletableFuture().get();
            } else {
                String query = "MATCH (n:" + label + ")-[r:connect]->(n1:" + label +
                        ") WHERE n.id = $fromUUID AND n1.id = $toUUID " + "RETURN n,n1";

                statement = new Statement(query, parameters);
                result = transaction.run(statement);
                recordSize = result.list().size();
                result.consume();
                if (recordSize == 0) { // nodes relation doesn't exists
                    isUpserted = createRelationshipBetweenTwoNodes(nodeFrom, nodeTo, transaction, parameters);
                } else {
                    if (logger.isDebugEnabled())
                        logger.debug(nodeTo.getId(), nodeFrom.getId());
                    isUpserted = updateRelationshipBetweenTwoNodes(nodeTo, nodeFrom, statement, result, transaction, recordSize, relationProperties);
                }
                transaction.commitAsync().toCompletableFuture().get();
            }
        } catch (ClientException e) {
            logger.error("user relation creation failed : ", e);
            return Boolean.FALSE;

        }
        return isUpserted;
    }

    private Boolean updateRelationshipBetweenTwoNodes(Node nodeTo, Node nodeFrom, Statement statement, StatementResult result, Transaction transaction, int recordSize, Map<String, String> relationProperties) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.FROM_UUID, nodeFrom.getId());
        parameters.put(Constants.TO_UUID, nodeTo.getId());
        parameters.put(Constants.Graph.PROPS.getValue(), relationProperties);
        String updateQuery = "MATCH (n:" + label + ")-[r:connect]->(n1:" + label +
                ") WHERE n.id = $fromUUID AND n1.id = $toUUID " + "SET r" + " += " +
                "$props " + "RETURN n,n1";

        statement = new Statement(updateQuery, parameters);
        result = transaction.run(statement);
        recordSize = result.list().size();
        result.consume();
        if (recordSize == 0) {
            logger.info("user relation with toUUID {} and fromUUID {} in updateRelationshipBetweenTwoNodes failed to update ", nodeTo.getId(),
                    nodeFrom.getId());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private Boolean createRelationshipBetweenTwoNodes(Node nodeFrom, Node nodeTo, Transaction transaction, Map<String, Object> parameters) {
        int recordSize;
        StatementResult result;
        StringBuilder query;
        Statement statement;
        query = new StringBuilder();
        query.append("MATCH (n:").append(label).append("), (n1:").append(label)
                .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID ")
                .append("CREATE (n)-[r:connect]->(n1) ").append("SET r").append(" += ").append("$props ")
                .append("RETURN n,n1");

        statement = new Statement(query.toString(), parameters);
        result = transaction.run(statement);
        recordSize = result.list().size();
        result.consume();
        if (recordSize == 0) {
            logger.info("user relation with toUUID {} and fromUUID {} in createRelationshipBetweenTwoNodes failed to create ", nodeTo.getId(),
                    nodeFrom.getId());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public int getNeighboursCount(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction) {
        int count;

        try (Session session = neo4jDriver.session()) {
            try (Transaction transaction = session.beginTransaction()) {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put(Constants.Graph.UUID.getValue(), UUID);
                parameters.put(Constants.Graph.PROPS.getValue(), relationProperties);

                StringBuilder query = new StringBuilder();

                if (direction == Constants.DIRECTION.OUT) {
                    query.append("MATCH (n:").append(label).append(")-[r:connect]->(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                } else if (direction == Constants.DIRECTION.IN) {
                    query.append("MATCH (n:").append(label).append(")<-[r:connect]-(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                } else {
                    query.append("MATCH (n:").append(label).append(")-[r:connect]-(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                }

                relationProperties.forEach((key, value) -> query.append(" AND r.").append(key).append(" = ").append("'").append(value).append("'"));
                query.append(" RETURN count(*)");
                Statement statement = new Statement(query.toString(), parameters);

                StatementResult result = transaction.run(statement);
                List<Record> records = result.list();
                result.consume();
                count = records.get(0).get("count(*)").asInt();
                logger.info("{} nodes count.", count);

            } catch (ClientException e) {
                throw new GraphException(ErrorCode.GRAPH_TRANSACTIONAL_ERROR.name(), e.getMessage());

            }
        } catch (SessionExpiredException se) {
            throw new GraphException(ErrorCode.GRAPH_SESSION_EXPIRED_ERROR.name(), se.getMessage());
        }
        return count;

    }

    private List<Node> getNodes(List<Record> records) {
        List<Node> nodes = new ArrayList<>();
        if (records.size() > 0) {
            for (Record record : records) {

                // TODO: optimise

                String id = null;
                for (String k : record.keys()) {
                    org.neo4j.driver.v1.types.Type t = record.get(k).type();
                    if (t.equals(TYPE_SYSTEM.NODE())) {
                        org.neo4j.driver.v1.types.Node node = record.get(k).asNode();
                        if (node.get(Constants.Graph.ID.getValue()) == null)
                            throw new GraphException(ErrorCode.MISSING_PROPERTY_ERROR.name(),
                                    "Missing {id} mandatory field");
                        id = node.get(Constants.Graph.ID.getValue()).asString();
                    } else if (t.equals(TYPE_SYSTEM.STRING()) && k.contains(Constants.Graph.ID.getValue())) {
                        id = record.get(k).asString();

                    } else {
                        throw new GraphException(ErrorCode.MISSING_PROPERTY_ERROR.name(),
                                "Missing {id} mandatory field");
                    }

                }
                Node nodePojo = new Node(id);
                nodes.add(nodePojo);

            }
        }
        return nodes;
    }

    public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction,
                                    int level, int offset, int limit, List<String> attributes) {
        try (Session session = neo4jDriver.session()) {
            Transaction transaction = session.beginTransaction();
            try {

                if (level == 0)
                    throw new GraphException(ErrorCode.RECORD_NOT_FOUND_ERROR.name(), "Oth level have no neighbours ");

                Map<String, Object> parameters = new HashMap<>();
                parameters.put(Constants.UUID, UUID);
                parameters.put(Constants.PROPS, relationProperties);

                StringBuilder linkNthLevel = new StringBuilder();
                for (int i = 0; i <= level; i++) {
                    if (direction == Constants.DIRECTION.OUT)
                        linkNthLevel.append("(n").append(i).append(":").append(label).append(")").append("-[r")
                                .append(i).append(":connect]->");
                    if (direction == Constants.DIRECTION.IN)
                        linkNthLevel.append("(n").append(i).append(":").append(label).append(")").append("<-[r")
                                .append(i).append(":connect]-");
                    if (direction == null)
                        linkNthLevel.append("(n").append(i).append(":").append(label).append(")").append("-[r")
                                .append(i).append(":connect]-");
                }
                String s = (direction == Constants.DIRECTION.IN)
                        ? linkNthLevel.substring(0, linkNthLevel.lastIndexOf("[") - 2)
                        : linkNthLevel.substring(0, linkNthLevel.lastIndexOf("[") - 1);

                StringBuilder query = new StringBuilder();
                query.append("MATCH ").append(s).append(" WHERE n0.id = $UUID ");

                relationProperties.forEach((key, value) -> query.append(" AND r").append(level - 1).append(".")
                        .append(key).append(" = ").append("'").append(value).append("'"));
                query.append(" RETURN ");
                StringBuilder sb = new StringBuilder();
                if (!CollectionUtils.isEmpty(attributes)) {
                    attributes.forEach(
                            attribute -> sb.append("n").append(level).append(".").append(attribute).append(","));
                    sb.deleteCharAt(sb.length() - 1);
                } else {
                    sb.append("n").append(level);
                }
                query.append(sb).append(" Skip ").append(offset).append(" limit ").append(limit);

                Statement statement = new Statement(query.toString(), parameters);

                StatementResult result = transaction.run(statement);
                List<Record> records = result.list();

                transaction.commitAsync().toCompletableFuture().get();
                logger.info("Neighbour users for UUID {} found successfully ", UUID);
                return getNodes(records);

            } catch (ClientException e) {
                transaction.rollbackAsync().toCompletableFuture();
                throw new GraphException(ErrorCode.GRAPH_TRANSACTIONAL_ERROR.name(), e.getMessage());

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                transaction.close();
            }
        } catch (SessionExpiredException se) {
            throw new GraphException(ErrorCode.GRAPH_SESSION_EXPIRED_ERROR.name(), se.getMessage());
        }

    }

}
