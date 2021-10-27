package org.sunbird.hubservices.daoimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.exception.ErrorCode;
import org.sunbird.cb.hubservices.exception.GraphException;
import org.sunbird.cb.hubservices.exception.ValidationException;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.hubservices.dao.IGraphDao;

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

    @Override
    public void upsertNode(Node node) {
        Session session = neo4jDriver.session();
        try {
            Transaction transaction = session.beginTransaction();
            try {
                if (Objects.isNull(node) || StringUtils.isBlank(node.getIdentifier())) {
                    throw new ValidationException("Node identifier cannot be empty");
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

            } catch (ClientException e) {
                transaction.rollbackAsync().toCompletableFuture();
                if (e.getMessage().contains("already exists"))
                    logger.error("user node upsertion failed : {}", e.getMessage());
                else throw new GraphException(ErrorCode.GRAPH_TRANSACTIONAL_ERROR.name(), e.getMessage());

            } finally {
                transaction.close();
            }

        } catch (SessionExpiredException se) {
            throw new GraphException(ErrorCode.GRAPH_SESSION_EXPIRED_ERROR.name(), se.getMessage());
        } finally {
            session.close();
        }
    }

    @Override
    public void upsertRelation(String fromUUID, String toUUID, Map<String, String> relationProperties) {
        Session session = neo4jDriver.session();
        try {
            Transaction transaction = session.beginTransaction();
            try {
                if (StringUtils.isBlank(fromUUID) || StringUtils.isBlank(toUUID) || CollectionUtils.isEmpty(relationProperties)) {
                    throw new ValidationException("UUIDs or properties cannot be empty");
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
                    throw new GraphException(ErrorCode.RECORD_NOT_FOUND_ERROR.name(), "users with toUUID {" + toUUID + "} or fromUUID {" + fromUUID + "} not found");
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
            } catch (ClientException e) {
                transaction.rollbackAsync().toCompletableFuture();
                throw new GraphException(ErrorCode.GRAPH_TRANSACTIONAL_ERROR.name(), e.getMessage());

            } finally {
                transaction.close();
            }
        } catch (SessionExpiredException se) {
            throw new GraphException(ErrorCode.GRAPH_SESSION_EXPIRED_ERROR.name(), se.getMessage());
        } finally {
            session.close();
        }

    }

    @Override
    public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int limit) {
        Session session = neo4jDriver.session();
        try {
            Transaction transaction = session.beginTransaction();
            try {
                if (CollectionUtils.isEmpty(relationProperties) || StringUtils.isBlank(UUID)) {
                    throw new ValidationException("UUID, property, direction cannot be empty");
                }

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("UUID", UUID);
                parameters.put("props", relationProperties);

                StringBuilder query = new StringBuilder();

                if (direction == Constants.DIRECTION.OUT) {
                    query.append("MATCH (n:").append(label).append(")-[r:connect]->(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                } else if(direction == Constants.DIRECTION.IN) {
                    query.append("MATCH (n:").append(label).append(")<-[r:connect]-(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                } else {
                    query.append("MATCH (n:").append(label).append(")-[r:connect]-(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                }

                relationProperties.entrySet().forEach(r -> query.append(" AND r.").append(r.getKey()).append(" = ").append("'" + r.getValue() + "'"));
                query.append(" RETURN n1").append(" Skip ").append(offset).append(" limit ").append(limit);

                Statement statement = new Statement(query.toString(), parameters);

                StatementResult result = transaction.run(statement);
                List<Record> records = result.list();

                transaction.commitAsync().toCompletableFuture();
                logger.info("Neighbour users for UUID {} found successfully ", UUID);
                return getNodes(records);

            } catch (ClientException e) {
                transaction.rollbackAsync().toCompletableFuture();
                throw new GraphException(ErrorCode.GRAPH_TRANSACTIONAL_ERROR.name(), e.getMessage());

            } finally {
                transaction.close();
            }
        } catch (SessionExpiredException se) {
            throw new GraphException(ErrorCode.GRAPH_SESSION_EXPIRED_ERROR.name(), se.getMessage());
        } finally {
            session.close();
        }

    }

    @Override
    public int getNeighboursCount(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction) {
        int count = 0;

        Session session = neo4jDriver.session();
        try {
            Transaction transaction = session.beginTransaction();
            try {
                if (StringUtils.isBlank(UUID)) {
                    throw new ValidationException("UUID cannot be empty");
                }

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("UUID", UUID);
                parameters.put("props", relationProperties);

                StringBuilder query = new StringBuilder();

                if (direction == Constants.DIRECTION.OUT) {
                    query.append("MATCH (n:").append(label).append(")-[r:connect]->(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                } else if(direction == Constants.DIRECTION.IN) {
                    query.append("MATCH (n:").append(label).append(")<-[r:connect]-(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                } else {
                    query.append("MATCH (n:").append(label).append(")-[r:connect]-(n1:").append(label)
                            .append(") WHERE n.id = $UUID ");
                }

                relationProperties.entrySet().forEach(r -> query.append(" AND r.").append(r.getKey()).append(" = ").append("'" + r.getValue() + "'"));
                query.append(" RETURN r");

                Statement statement = new Statement(query.toString(), parameters);

                StatementResult result = transaction.run(statement);
                List<Record> records = result.list();

                transaction.commitAsync().toCompletableFuture();
                if (records.size() > 0) {
                    for (Record record : records) {
                        count = record.get("count(r)").asInt();

                    }
                }
                logger.info("{} nodes count.", count);

            } catch (ClientException e) {
                transaction.rollbackAsync().toCompletableFuture();
                throw new GraphException(ErrorCode.GRAPH_TRANSACTIONAL_ERROR.name(), e.getMessage());

            } finally {
                transaction.close();
            }
        } catch (SessionExpiredException se) {
            throw new GraphException(ErrorCode.GRAPH_SESSION_EXPIRED_ERROR.name(), se.getMessage());
        } finally {
            session.close();
        }
        return count;

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

}
