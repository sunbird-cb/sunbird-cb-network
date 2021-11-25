package org.sunbird.cb.hubservices;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.SessionExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.exception.ErrorCode;
import org.sunbird.cb.hubservices.exception.GraphException;
import org.sunbird.cb.hubservices.exception.ValidationException;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.service.IGraphService;
import org.sunbird.cb.hubservices.service.INodeService;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.hubservices.dao.IGraphDao;

import java.util.*;

@Component
public class AppStartupRunner implements ApplicationRunner {


	@Autowired
    private IGraphDao userGraphDao;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private IGraphService graphService;


	@Autowired
    ObjectMapper mapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        Node n1 = new Node("2001","node1","D1");
        n1.setUpdatedAt(new Date());
        Node n2 = new Node("2002","node1","D1");
        n1.setUpdatedAt(new Date());

        Node n3 = new Node("2003","node3",null);
        Node n4 = new Node("2004","node4",null);
        Node n5 = new Node("2005","node5",null);



        Map<String,String> relP = new HashMap<>();
        relP.put("status", "pending");
        relP.put("desc", "init connection");

        Map<String,String> relA = new HashMap<>();
        relA.put("status", "approved");
        relA.put("desc", "approved connection");


        Map<String, Object> parameters = new HashMap<>();
        parameters.put("UUID", "1001");
        parameters.put("props", relA);





        List<Integer> listOfNumbers = Arrays.asList(1, 2, 3, 4, 5,6,7,8,9,10);
        //List<Integer> listOfNumbers = Arrays.asList(1, 2, 3, 4, 5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20);

/*        listOfNumbers.parallelStream().forEach(number ->{
                    try{
                        //nodeService.connect(n1, n2, relP);

                        userGraphDao.upsertNode(n1);
                        userGraphDao.upsertNode(n2);

                    }catch (GraphException e){
                        System.out.println("userGraphDao.upsertNode Caller exce msg:"+e.getMessage());

                    }
                }
        );

        long startTime = System.nanoTime();

        listOfNumbers.parallelStream().forEach(number ->{
            try{
                //nodeService.connect(n1, n2, relP);

                userGraphDao.upsertRelation(n1.getIdentifier(), n2.getIdentifier(), relP);

            }catch (GraphException e){
                System.out.println("userGraphDao.upsertRelation Caller exce msg:"+e.getMessage());

            }
        }
        );

        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        System.out.println("nodeService.connect time taken (sec)"+(double)duration/1000000000);*/

/*        long startTime1 = System.nanoTime();

        listOfNumbers.parallelStream().forEach(number ->{
            try{
                graphService.createNodeWithRelation(n1, n2, "Pending");

            }catch (Exception e){
                System.out.println("graphService.createNodeWithRelation Caller exce msg:"+e.getMessage());

            }
        });
        long endTime1 = System.nanoTime();

        long duration1 = (endTime1 - startTime1);
        System.out.println("createNodeWithRelation time taken(sec) "+(double)duration1/1000000000);*/



        //userGraphDao.upsertRelation("2001", "2006", relP);

//        nodeService.connect(n1, n2, relP);
//        nodeService.connect(n1, n3, relP);
//        nodeService.connect(n2, n4, relP);
//        nodeService.connect(n4, n5, relP);



/*
        List<Node> nodeA=nodeService.getNodeByOutRelation("2001", relP, 0, 10);
        System.out.println("nx nodeA size"+nodeA.size());

        List<Node> nodeB=nodeService.getNodeByOutRelation("2001", relA, 0, 10);
        System.out.println("nx nodeB size"+nodeB.size());*/

        List<Node> nodes=nodeService.getNodeNextLevel("2001", relP,0,10);
        System.out.println("nx nodes size"+nodes.size());
        nodes.forEach(n->{
            System.out.println("node->"+n.getIdentifier());
        });


        List<Node> allnodes=nodeService.getAllNodes("2001", relP,0,10);
        System.out.println("allnodes nodes size"+allnodes.size());
        allnodes.forEach(n->{
            System.out.println("node->"+n.getIdentifier());
        });

        int count = nodeService.getNodesCount("2001", relP, null);
        System.out.println("totol count->"+count);






 /*       //create records
        userGraphDao.upsertNode(n1);
        userGraphDao.upsertNode(n2);
        userGraphDao.upsertNode(n3);
        userGraphDao.upsertNode(n4);

        Map<String,String> relP = new HashMap<>();
        relP.put("status", "pending");
        relP.put("desc", "init connection");

        Map<String,String> relA = new HashMap<>();
        relA.put("status", "approved");
        relA.put("desc", "approved connection");

        //create relations
        userGraphDao.upsertRelation("2001", "2002", relP);
        userGraphDao.upsertRelation("2003", "2004", relP);
        userGraphDao.upsertRelation("2001", "2003", relP);
        userGraphDao.upsertRelation("2002", "2004", relP);
        userGraphDao.upsertRelation("2002", "2004", relA);*/
        //userGraphDao.upsertRelation("2001", "2011", "pending");




        //find all adjacent nodes
/*       List<Record> recordList1 = userGraphDao.getAllNeighbours("2001");
        for(Record r:recordList1){
            System.out.println("2001 all neighbours "+r);
        }
        List<Record> recordList2 = userGraphDao.getAllNeighbours("2002");
        for(Record r:recordList2){
            System.out.println("2002 all neighbours "+r);
        }*/

        //find  adjacent nodes by relations
/*
        List<Node> recordList3 = userGraphDao.getNeighbours("2001", relP, Constants.DIRECTION.OUT);
        for(Node r:recordList3){
            System.out.println("2001 pending neighbours "+r.getIdentifier());
        }
        List<Node> recordList4 =userGraphDao.getNeighbours("2002",relA, Constants.DIRECTION.OUT);
        for(Node r:recordList4){
            System.out.println("2002 approved neighbours "+r.getIdentifier());
        }
*/


    }

//    public List<Node> getNeighbours(String UUID, Map<String, String> relationProperties, Constants.DIRECTION direction, int level, int offset, int limit) {
//        Session session = neo4jDriver.session();
//        try {
//            Transaction transaction = session.beginTransaction();
//            try {
//                if (CollectionUtils.isEmpty(relationProperties) || StringUtils.isBlank(UUID)) {
//                    throw new ValidationException("UUID, property, direction cannot be empty");
//                }
//
//                Map<String, Object> parameters = new HashMap<>();
//                parameters.put("UUID", UUID);
//                parameters.put("props", relationProperties);
//
//                StringBuilder linkNthLevel = new StringBuilder();
//                for(int i=1; i<=level; i++){
//                    if(direction == Constants.DIRECTION.OUT)
//                        linkNthLevel.append("-[r").append(i).append(":connect]->(n").append(i).append(":").append(label).append(")");
//                    if(direction == Constants.DIRECTION.IN)
//                        linkNthLevel.append("<-[r").append(i).append(":connect]-(n").append(i).append(":").append(label).append(")");
//                    else
//                        linkNthLevel.append("-[r").append(i).append(":connect]-(n").append(i).append(":").append(label).append(")");
//                }
//
//                StringBuilder query = new StringBuilder();
//
//                query.append("MATCH (n:").append(label).append(")").append(linkNthLevel)
//                        .append(" WHERE n.id = $UUID ");
//
//
//                relationProperties.entrySet().forEach(r -> query.append(" AND r").append(1).append(".").append(r.getKey()).append(" = ").append("'" + r.getValue() + "'"));
//                query.append(" RETURN n").append(level).append(" Skip ").append(offset).append(" limit ").append(limit);
//
//                Statement statement = new Statement(query.toString(), parameters);
//
//                StatementResult result = transaction.run(statement);
//                List<Record> records = result.list();
//
//                transaction.commitAsync().toCompletableFuture();
//                logger.info("Neighbour users for UUID {} found successfully ", UUID);
//                return getNodes(records);
//
//            } catch (ClientException e) {
//                transaction.rollbackAsync().toCompletableFuture();
//                throw new GraphException(ErrorCode.GRAPH_TRANSACTIONAL_ERROR.name(), e.getMessage());
//
//            } finally {
//                transaction.close();
//            }
//        } catch (SessionExpiredException se) {
//            throw new GraphException(ErrorCode.GRAPH_SESSION_EXPIRED_ERROR.name(), se.getMessage());
//        } finally {
//            session.close();
//        }
//
//    }

}
