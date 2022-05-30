package org.sunbird.cb.hubservices.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.SessionExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.service.IGraphService;
import org.sunbird.cb.hubservices.util.Constants;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GraphService implements IGraphService {

    private Logger logger = LoggerFactory.getLogger(GraphService.class);

    @Autowired
    Driver neo4jDriver;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public Boolean createNodeWithRelation(Node from, Node to, String relation) throws Exception {

        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();
        Map<String, Object> params = new HashMap<>();

        try {

            params.put(Constants.DATA, mapper.convertValue(from, Map.class));
            ((Map) params.get(Constants.DATA)).put(Constants.STATUS, relation);
            params.put(Constants.CHILD_DATA, mapper.convertValue(to, Map.class));

            String parentProperties = "{identifier:{data}.identifier, name:{data}.name, department:{data}.department}";
            String childProperties = "{identifier:{childData}.identifier, name:{childData}.name, department:{childData}.department}";
            String relProperties = "{updatedAt:{data}.updatedAt, status:{data}.status}";

            String text1 = "merge (n: user" + parentProperties + ") ";
            String text01 = "merge (n1: user" + childProperties + ") ";
            String text2 = "merge (n)-[r:" + relation + relProperties + "]->(n1) return n";
            Statement statement = new Statement(text1 + text01 + text2, params);
            logger.info("Merge Cypher query:: " + statement.text());

            StatementResult result = transaction.run(statement);
            List<Record> records = result.list();

            if (records == null || records.isEmpty()) {
                throw new Exception("Something went wrong");
            }
            transaction.commitAsync().toCompletableFuture().get();
            logger.info("user node with relation created successfully ");

        } catch (Exception e) {
            transaction.rollbackAsync().toCompletableFuture().get();
            logger.error("user node creation failed : ", e);
            return Boolean.FALSE;

        } finally {
            transaction.close();
            session.close();
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteRelation(Node from, Node to, String relation) throws Exception {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();
        Map<String, Object> params = new HashMap<>();

        try {

            params.put(Constants.DATA, mapper.convertValue(from, Map.class));
            params.put(Constants.CHILD_DATA, mapper.convertValue(to, Map.class));

            String text02 = "MATCH (:user {name:{data}.name})-[r]-(:user {name:{childData}.name}) DELETE r ";
            Statement statement1 = new Statement(text02, params);
            transaction.run(statement1);
            transaction.commitAsync().toCompletableFuture().get();
            logger.info("user node relation deleted successfully ");

        } catch (Exception e) {
            transaction.rollbackAsync().toCompletableFuture().get();
            logger.error("user delete relation failed : {}", e);
            return Boolean.FALSE;
        } finally {
            transaction.close();
            session.close();
        }
        return Boolean.TRUE;
    }

    @Override
    public List<Node> getNodesInEdge(String identifier, String relation, int offset, int size) throws Exception {

        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        List<Node> nodes = new ArrayList<>();
        try {

            // String text = "MATCH (n)<-[r:"+relation+"]-(n1) WHERE n.identifier =
            // '"+identifier+"' RETURN n1 ORDER BY updatedAt DESC Skip "+offset+ " LIMIT "
            // +size;
            String text = "MATCH (n)<-[r:" + relation + "]-(n1) WHERE n.identifier = '" + identifier
                    + "' RETURN n1 ORDER BY r.updatedAt DESC Skip " + offset + " limit " + size;
            logger.info("text:: {}", text);
            Statement statement = new Statement(text);

            StatementResult result = transaction.run(statement);
            List<Record> records = result.list();
            nodes = getNodes(records);

        } catch (Exception e) {
            transaction.rollbackAsync().toCompletableFuture().get();
            logger.error("Fetching user node failed : ", e);

        } finally {
            transaction.close();
            session.close();
        }
        return nodes;
    }

    @Override
    public List<Node> getNodesOutEdge(String identifier, String relation, int offset, int size) throws Exception {

        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        List<Node> nodes = new ArrayList<>();
        try {

            // String text = "MATCH (n)<-[r:"+relation+"]-(n1) WHERE n.identifier =
            // '"+identifier+"' RETURN n1 ORDER BY updatedAt DESC Skip "+offset+ " LIMIT "
            // +size;
            String text = "MATCH (n)-[r:" + relation + "]->(n1) WHERE n.identifier = '" + identifier
                    + "' RETURN n1 ORDER BY r.updatedAt DESC Skip " + offset + " limit " + size;
            logger.info("text:: {}", text);
            Statement statement = new Statement(text);

            StatementResult result = transaction.run(statement);
            List<Record> records = result.list();

            nodes = getNodes(records);

        } catch (Exception e) {
            transaction.rollbackAsync().toCompletableFuture().get();
            logger.error("Fetching user node failed : ", e);

        } finally {
            transaction.close();
            session.close();
        }
        return nodes;
    }

    @Override
    public List<Node> getNodesInAndOutEdge(String identifier, String relation, int offset, int size) throws Exception {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        List<Node> nodes = new ArrayList<>();
        try {

            // String text = "MATCH (n)<-[r:"+relation+"]-(n1) WHERE n.identifier =
            // '"+identifier+"' RETURN n1 ORDER BY updatedAt DESC Skip "+offset+ " LIMIT "
            // +size;
            String text = "MATCH (n)-[r:" + relation + "]-(n1) WHERE n.identifier = '" + identifier
                    + "' RETURN n1 ORDER BY r.updatedAt DESC Skip " + offset + " limit " + size;
            logger.info("text:: {}", text);
            Statement statement = new Statement(text);

            StatementResult result = transaction.run(statement);
            List<Record> records = result.list();

            nodes = getNodes(records);

        } catch (Exception e) {
            transaction.rollbackAsync().toCompletableFuture().get();
            logger.error("Fetching user node failed : ", e);

        } finally {
            transaction.close();
            session.close();
        }
        return nodes;
    }

    @Override
    public List<Node> getNodesNextLevel(String identifier, String relation, int offset, int size) throws Exception {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();

        List<Node> nodes = new ArrayList<>();
        try {

            // TODO generic for any level
            // String text = "MATCH (n)<-[r:"+relation+"]-(n1) WHERE n.identifier =
            // '"+identifier+"' RETURN n1 ORDER BY r1.updatedAt DESC Skip "+offset+ " LIMIT
            // " +size;
            String text = "MATCH (n)-[r:" + relation + "]-(n0)-[r1:" + relation + "]-(n1) WHERE n.identifier = '"
                    + identifier + "' RETURN n1  Skip " + offset + " limit " + size;
            logger.info("text:: {}", text);
            Statement statement = new Statement(text);

            StatementResult result = transaction.run(statement);
            List<Record> records = result.list();

            nodes = getNodes(records);

        } catch (Exception e) {
            transaction.rollbackAsync().toCompletableFuture().get();
            logger.error("Fetching user node failed : ", e);

        } finally {
            transaction.close();
            session.close();
        }
        return nodes;
    }

    private List<Node> getNodes(List<Record> records) {

        List<Node> nodes = new ArrayList<>();

        if (records.size() > 0) {
            logger.info("{} User node fetched.", records.size());
            for (Record record : records) {
                org.neo4j.driver.v1.types.Node node = record.get("n1").asNode();
                // TODO: optimise
                Node nodePojo = new Node(node.get("identifier").asString(), node.get("name").asString(),
                        node.get("department").asString());
                // Date d =
                // Date.from(node.get("updatedAt").asLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                // nodePojo.setUpdatedAt(d);
                logger.info("########### nodePojo fetched {}", nodePojo);

                nodes.add(nodePojo);

            }
        }
        return nodes;
    }

    @Override
    public int getAllNodeCount(String identifier, String relation, Constants.DIRECTION direction) throws Exception {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();
        int count = 0;
        String text = null;
        try {

            if (direction == null) {
                text = "MATCH (n)-[r:" + relation + "]-(n1) WHERE n.identifier = '" + identifier + "' RETURN count(r) ";

            } else if (direction.equals(Constants.DIRECTION.IN)) {
                text = "MATCH (n)<-[r:" + relation + "]-(n1) WHERE n.identifier = '" + identifier
                        + "' RETURN count(r) ";

            } else if (direction.equals(Constants.DIRECTION.OUT)) {
                text = "MATCH (n)-[r:" + relation + "]->(n1) WHERE n.identifier = '" + identifier
                        + "' RETURN count(r) ";

            }
            // String text = "MATCH (n)<-[r:"+relation+"]-(n1) WHERE n.identifier =
            // '"+identifier+"' RETURN n1 ORDER BY updatedAt DESC Skip "+offset+ " LIMIT "
            // +size;
            logger.info("text:: {}", text);
            Statement statement = new Statement(text);

            StatementResult result = transaction.run(statement);
            List<Record> records = result.list();

            if (records.size() > 0) {
                logger.info("{} User node fetched.", records.size());
                for (Record record : records) {
                    count = record.get("count(r)").asInt();

                }
            }

        } catch (Exception e) {
            transaction.rollbackAsync().toCompletableFuture().get();
            logger.error("Fetching user node failed : ", e);

        } finally {
            transaction.close();
            session.close();
        }

        return count;
    }

    @Override
    public List<Node> getAllNodes(String identifier) throws Exception {
        Session session = neo4jDriver.session();
        Transaction transaction = session.beginTransaction();
        List<Node> nodes = new ArrayList<>();

        try {

            // String text = "MATCH (n)<-[r:"+relation+"]-(n1) WHERE n.identifier =
            // '"+identifier+"' RETURN n1 ORDER BY updatedAt DESC Skip "+offset+ " LIMIT "
            // +size;
            String text = "MATCH (n)-[r]-(n1) WHERE n.identifier = '" + identifier + "' RETURN n1 ";
            logger.info("text:: {}", text);
            Statement statement = new Statement(text);

            StatementResult result = transaction.run(statement);
            List<Record> records = result.list();

            nodes = getNodes(records);
        } catch (Exception e) {
            transaction.rollbackAsync().toCompletableFuture().get();
            logger.error("Fetching user node failed : ", e);

        } finally {
            transaction.close();
            session.close();
        }

        return nodes;
    }

    @Override
    public List<Node> getNodes(String identifier, Map<String, String> relationProperties, Constants.DIRECTION direction,
                               int offset, int size, List<String> attributes) {
        checkParams(identifier, relationProperties);
        return getNodesWith(identifier, relationProperties, direction, offset, size, attributes);
    }

    private List<Node> getNodesWith(String identifier, Map<String, String> relationProperties,
                                    Constants.DIRECTION direction, int offset, int size, List<String> attributes) {
        return getNeighbours(identifier, relationProperties, direction, 1, offset, size, attributes);
    }

    private void checkParams(String identifier, Map<String, String> relationProperties) {
        if (StringUtils.isEmpty(identifier) || CollectionUtils.isEmpty(relationProperties)) {
            throw new ValidationException("identifier or relation properties cannot be empty");
        }
    }

    public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction,
                                    int level, int offset, int limit, List<String> attributes) {
        Session session = neo4jDriver.session();
        try {
            Transaction transaction = session.beginTransaction();
            try {

                if (level == 0)
                    logger.info("Oth level have no neighbours ");

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("UUID", UUID);
                parameters.put("props", relationProperties);

                StringBuilder linkNthLevel = new StringBuilder();
                for (int i = 0; i <= level; i++) {
                    if (direction == Constants.DIRECTION.OUT)
                        linkNthLevel.append("(n").append(i).append(":").append("user").append(")").append("-[r")
                                .append(i).append(":connect]->");
                    if (direction == Constants.DIRECTION.IN)
                        linkNthLevel.append("(n").append(i).append(":").append("user").append(")").append("<-[r")
                                .append(i).append(":connect]-");
                    if (direction == null)
                        linkNthLevel.append("(n").append(i).append(":").append("user").append(")").append("-[r")
                                .append(i).append(":connect]-");
                }
                String s = (direction == Constants.DIRECTION.IN)
                        ? linkNthLevel.substring(0, linkNthLevel.lastIndexOf("[") - 2)
                        : linkNthLevel.substring(0, linkNthLevel.lastIndexOf("[") - 1);

                StringBuilder query = new StringBuilder();
                query.append("MATCH ").append(s).append(" WHERE n0.id = $UUID ");

                relationProperties.entrySet().forEach(r -> query.append(" AND r").append(level - 1).append(".")
                        .append(r.getKey()).append(" = ").append("'" + r.getValue() + "'"));
                query.append(" RETURN ");
                StringBuilder sb = new StringBuilder();
                if (!CollectionUtils.isEmpty(attributes)) {
                    attributes.stream().forEach(
                            attribute -> sb.append("n").append(level).append(".").append(attribute).append(","));
                    sb.deleteCharAt(sb.length() - 1);
                } else {
                    sb.append("n").append(level);
                }
                query.append(sb).append(" Skip ").append(offset).append(" limit ").append(limit);

                System.out.println("query-> " + query);
                Statement statement = new Statement(query.toString(), parameters);

                StatementResult result = transaction.run(statement);
                List<Record> records = result.list();

                transaction.commitAsync().toCompletableFuture();
                logger.info("Neighbour users for UUID {} found successfully ", UUID);
                return getNodes(records);

            } catch (ClientException e) {
                transaction.rollbackAsync().toCompletableFuture();
                logger.error(e.getMessage());

            } finally {
                transaction.close();
            }
        } catch (SessionExpiredException se) {
            logger.error(se.getMessage());
        } finally {
            session.close();
        }
        return new ArrayList<>();
    }


}
