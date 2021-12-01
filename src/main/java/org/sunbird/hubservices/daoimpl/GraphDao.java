package org.sunbird.hubservices.daoimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.exception.ErrorCode;
import org.sunbird.cb.hubservices.exception.GraphException;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.hubservices.dao.IGraphDao;

import java.util.*;

import static org.neo4j.driver.internal.types.InternalTypeSystem.TYPE_SYSTEM;

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

                Map<String, Object> props = new HashMap<>();
                props = new ObjectMapper().convertValue(node, Map.class);

                Map<String, Object> params = new HashMap<>();
                params.put(Constants.Graph.PROPS.getValue(), props);

                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("CREATE (n:").append(label).append(") SET n = $props RETURN n");

                Statement statement = new Statement(queryBuilder.toString(), params);

                StatementResult result = transaction.run(statement);
                result.consume();
                transaction.commitAsync();
                logger.info("user node with id {} created successfully ", node.getId());

            } catch (ClientException e) {
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
    public void upsertRelation(Node nodeFrom, Node nodeTo, Map<String, String> relationProperties) {
        Session session = neo4jDriver.session();
        try {
            Transaction transaction = session.beginTransaction();
            try {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("fromUUID", nodeFrom.getId());
                parameters.put("toUUID", nodeTo.getId());
                parameters.put(Constants.Graph.PROPS.getValue(), relationProperties);

                StringBuilder query = new StringBuilder();
                query.append("MATCH (n:").append(label).append(")-[r:connect]->(n1:").append(label)
                        .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID ").append("SET r").append(" = ")
                        .append("$props ").append("RETURN n,n1");

                Statement statement = new Statement(query.toString(), parameters);
                StatementResult result = transaction.run(statement);
                int recordSize = result.list().size();
                result.consume();
                if (recordSize == 0) { // nodes relation doesnot exists

                    query = new StringBuilder();
                    query.append("MATCH (n:").append(label).append("), (n1:").append(label)
                            .append(") WHERE n.id = $fromUUID AND n1.id = $toUUID ").append("CREATE (n)-[r:connect]->(n1) ")
                            .append("SET r").append(" = ").append("$props ").append("RETURN n,n1");

                    statement = new Statement(query.toString(), parameters);
                    result = transaction.run(statement);
                    recordSize = result.list().size();
                    result.consume();
                    if(recordSize == 0){
                        logger.info("user relation with toUUID {} and fromUUID {} failed to update ", nodeTo.getId(), nodeFrom.getId());
                        throw new GraphException(ErrorCode.GRAPH_TRANSACTIONAL_ERROR.name(), "user relation with "+nodeTo.getId()+" and "+nodeFrom.getId()+" failed to update ");
                    }

                }
                transaction.commitAsync();
                logger.info("user relation with toUUID {} and fromUUID {} updated successfully ", nodeTo.getId(), nodeFrom.getId());

            } catch (ClientException e) {
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
    public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction, int offset, int limit, List<String> attributes) {
        Session session = neo4jDriver.session();
        try {
            Transaction transaction = session.beginTransaction();
            try {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put(Constants.Graph.UUID.getValue(), UUID);
                parameters.put(Constants.Graph.PROPS.getValue(), relationProperties);

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
                query.append(" RETURN ");
                StringBuilder sb = new StringBuilder();
                if(!CollectionUtils.isEmpty(attributes)){
                    attributes.stream().forEach( attribute -> sb.append("n1.").append(attribute).append(","));
                    sb.deleteCharAt(sb.length()-1);
                } else {
                    sb.append("n1");
                }
                query.append(sb).append(" Skip ").append(offset).append(" limit ").append(limit);

                Statement statement = new Statement(query.toString(), parameters);

                StatementResult result = transaction.run(statement);
                List<Record> records = result.list();

                List<Node> nodes = getNodes(records);
                logger.info("Neighbour users for UUID {} found successfully ", UUID);
                return nodes;

            } catch (ClientException e) {
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

                Map<String, Object> parameters = new HashMap<>();
                parameters.put(Constants.Graph.UUID.getValue(), UUID);
                parameters.put(Constants.Graph.PROPS.getValue(), relationProperties);

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
                query.append(" RETURN count(*)");
                System.out.println("count query=="+query);
                Statement statement = new Statement(query.toString(), parameters);

                StatementResult result = transaction.run(statement);
                List<Record> records = result.list();
                result.consume();
                count = records.get(0).get("count(*)").asInt();
                logger.info("{} nodes count.", count);

            } catch (ClientException e) {
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

                // TODO: optimise

                String id = null;
                for(String k:record.keys()){
                    org.neo4j.driver.v1.types.Type t= record.get(k).type();
                    if(t.equals(TYPE_SYSTEM.NODE())){
                        org.neo4j.driver.v1.types.Node node = record.get(k).asNode();
                        if(node.get(Constants.Graph.ID.getValue())==null)
                            throw new GraphException(ErrorCode.MISSING_PROPERTY_ERROR.name(), "Missing {id} mandatory field");
                        id = node.get(Constants.Graph.ID.getValue()).asString();
                    } else if(t.equals(TYPE_SYSTEM.STRING()) && k.contains(Constants.Graph.ID.getValue())){
                        id = record.get(k).asString();

                    } else {
                         throw new GraphException(ErrorCode.MISSING_PROPERTY_ERROR.name(), "Missing {id} mandatory field");
                    }

                }
                Node nodePojo = new Node(id);
                nodes.add(nodePojo);

            }
        }
        return nodes;
    }

}
