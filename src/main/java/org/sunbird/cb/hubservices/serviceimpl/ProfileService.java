package org.sunbird.cb.hubservices.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.sunbird.cb.hubservices.exception.ApplicationException;
import org.sunbird.cb.hubservices.model.MultiSearch;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.model.Search;
import org.sunbird.cb.hubservices.profile.handler.ProfileUtils;
import org.sunbird.cb.hubservices.profile.handler.RegistryRequest;
import org.sunbird.cb.hubservices.service.IProfileService;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProfileService implements IProfileService {

    private Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    ConnectionProperties connectionProperties;

    @Autowired
    ConnectionService connectionService;

    @Autowired
    ObjectMapper mapper;



    @Override
    public Response findCommonProfile(String rootOrg, String userId, int offset, int limit) {

        Response responseConnections = connectionService.findSuggestedConnections(rootOrg, userId, offset, limit);
       return responseConnections;


    }

    @Override
    public Response findProfiles(String rootOrg, String userId, int offset, int limit) {

        Response responseConnections = connectionService.findAllConnectionsIdsByStatus(rootOrg, userId, Constants.Status.APPROVED, offset, limit);
        return responseConnections;

    }



    @Override
    public Response findProfileRequested(String rootOrg, String userId, int offset, int limit, Constants.DIRECTION direction) {
        Response responseConnections = connectionService.findConnectionsRequested(rootOrg, userId, offset, limit, direction);
        return  responseConnections;

    }


    //TODO: user wrapper layer to connect opensaber
    @Override
    public Response multiSearchProfiles(String rootOrg, String userId, MultiSearch mSearchRequest, String[] sourceFields) {


        Response response = new Response();
        try{
            List<String> connectionIdsToExclude = connectionService.findUserConnections(rootOrg, userId);
            connectionIdsToExclude.add(userId);
            logger.info("connectionIdsToExclude ->"+connectionIdsToExclude);

            logger.info("multisearch request -> "+mapper.writeValueAsString(mSearchRequest));

            List<String> tags = new ArrayList<>();

            Map<String, Object> tagRes = new HashMap<>();

            for(Search sRequest: mSearchRequest.getSearch()) {

                //Prepare of SearchDTO
                RegistryRequest registryRequest = new RegistryRequest();
                Map<String, Object> searchQueryMap = new HashMap<>();
                Map<String, Object> additionalProperties = new HashMap<>();
                additionalProperties.put(sRequest.getField(),sRequest.getValues().get(0));
                searchQueryMap.put("query","");
                searchQueryMap.put("filter",additionalProperties);
                searchQueryMap.put("offset",mSearchRequest.getOffset());
                searchQueryMap.put("limit",mSearchRequest.getSize());
                registryRequest.setRequest(searchQueryMap);
                tags.add(sRequest.getField());

                //Hit user search Api
                ResponseEntity responseEntity = ProfileUtils.getResponseEntity(connectionProperties.getLearnerServiceHost(), connectionProperties.getUserSearchEndPoint(), registryRequest);
                tagRes.put(sRequest.getField(), responseEntity.getBody());

            }
            logger.info("user search tagRes -->"+mapper.writeValueAsString(tagRes));

            List<Object> finalRes = new ArrayList<>();
            for(Map.Entry entry : tagRes.entrySet()){
                Map<String, Object> resObjects = new HashMap<>();
                resObjects.put("field",entry.getKey());
                resObjects.put("results", entry.getValue());
                finalRes.add(resObjects);
            }

            response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
            response.put(Constants.ResponseStatus.DATA, finalRes);
            response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);



        } catch (Exception e){
            logger.error(Constants.Message.CONNECTION_EXCEPTION_OCCURED, e);
            throw new ApplicationException(Constants.Message.FAILED_CONNECTION );

        }

        return response;
    }

}
