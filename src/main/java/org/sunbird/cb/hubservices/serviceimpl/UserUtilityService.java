package org.sunbird.cb.hubservices.serviceimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cb.hubservices.cache.RedisCacheMgr;
import org.sunbird.cb.hubservices.model.MultiSearch;
import org.sunbird.cb.hubservices.model.Request;
import org.sunbird.cb.hubservices.model.Search;
import org.sunbird.cb.hubservices.profile.handler.ProfileUtils;
import org.sunbird.cb.hubservices.service.IConnectionService;
import org.sunbird.cb.hubservices.service.IUserUtility;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.cb.hubservices.util.NetworkServerProperties;
import org.sunbird.cb.hubservices.util.PrettyPrintingMap;

import java.lang.reflect.Field;
import java.util.*;

@Service
public class UserUtilityService implements IUserUtility {

    @Autowired
    RedisCacheMgr redisCacheMgr;

    @Autowired
    ConnectionProperties connectionProperties;

    @Autowired
    IConnectionService connectionService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    NetworkServerProperties networkServerProperties;

    private Logger logger = LoggerFactory.getLogger(UserUtilityService.class);

    @Override
    public Map<String, Object> getUserInfoFromRedish(MultiSearch multiSearch, String[] sourceFields, List<String> connectionIdsToExclude) {
        String departmentName = "";
        List<String> includeFields = sourceFields != null && !Arrays.asList(sourceFields).isEmpty() ? Arrays.asList(sourceFields) : ProfileUtils.getUserDefaultFields();
        List<String> tags = new ArrayList<>();
        Map<String, Object> tagRes = new HashMap<>();
        for (Search sRequest : multiSearch.getSearch()) {
            departmentName = (String) sRequest.getValues().get(0);
            departmentName = departmentName.trim().replaceAll(" ", "");
            String userInformation = redisCacheMgr.getCache(Constants.USER_LIST + Constants.UNDER_SCORE + departmentName);
            if (!ObjectUtils.isEmpty(userInformation)) {
                try {
                    JsonNode jsonNode = mapper.readTree(userInformation);
                    ArrayNode arrayNode = (ArrayNode) jsonNode;
                    ArrayNode filteredArrayNode = mapper.createArrayNode();
                    for (JsonNode element : arrayNode) {
                        if (!connectionIdsToExclude.contains(element.get(ProfileUtils.Profile.USER_ID).asText())) {
                            filteredArrayNode.add(element);
                        }
                    }

                    int limit = getLimitRequest(multiSearch.getSize());
                    if (multiSearch.getSize() >= filteredArrayNode.size()) {
                        tagRes.put(sRequest.getField(), filteredArrayNode);
                    } else if (multiSearch.getSize() < filteredArrayNode.size()) {
                        Field innerArrayNode = ArrayNode.class.getDeclaredField("_children");
                        innerArrayNode.setAccessible(true);
                        List<JsonNode> innerArrayNodeChildNodes = (List<JsonNode>) innerArrayNode.get(filteredArrayNode);
                        List<JsonNode> limitedChildNodes = innerArrayNodeChildNodes.subList(0, multiSearch.getSize());
                        innerArrayNode.set(filteredArrayNode, limitedChildNodes);
                        tagRes.put(sRequest.getField(), filteredArrayNode);
                    }
                } catch (Exception e) {
                    tagRes.put(Constants.ResponseStatus.STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
                    logger.error(String.format("Error while connecting the nodes! error : %s", e.getMessage()));
                }
                return tagRes;
            } else {
                StringBuilder searchPath = new StringBuilder();
                searchPath.append(ProfileUtils.Profile.PROFILE_DETAILS).append(".").append(sRequest.getField());
                // Prepare of SearchDTO
                Request request = new Request();
                Map<String, Object> searchQueryMap = new HashMap<>();
                Map<String, Object> additionalProperties = new HashMap<>();
                additionalProperties.put(searchPath.toString(), sRequest.getValues().get(0));
                additionalProperties.put("status", 1);
                searchQueryMap.put("query", "");
                searchQueryMap.put("filters", additionalProperties);
                searchQueryMap.put("offset", multiSearch.getOffset());
                searchQueryMap.put("limit", getLimitRequest(multiSearch.getSize()));
                searchQueryMap.put("fields", includeFields);
                request.setRequest(searchQueryMap);
                tags.add(sRequest.getField());

                // Hit user search Api
                ResponseEntity<?> responseEntity = ProfileUtils.getResponseEntity(connectionProperties.getLearnerServiceHost(), connectionProperties.getUserSearchEndPoint(), request);
                JsonNode node = mapper.convertValue(responseEntity.getBody(), JsonNode.class);
                ArrayNode arrayRes = JsonNodeFactory.instance.arrayNode();
                ArrayNode nodes = (ArrayNode) node.get("result").get("response").get("content");
                for (JsonNode n : nodes) {
                    if (!connectionIdsToExclude.contains(n.get(ProfileUtils.Profile.USER_ID).asText())) {
                        JsonNode profileDetails = n.get(ProfileUtils.Profile.PROFILE_DETAILS);
                        if (!ObjectUtils.isEmpty(profileDetails.get(Constants.VERIFIED_KARMAYOGI))) {
                            ((ObjectNode) profileDetails).put(Constants.VERIFIED_KARMAYOGI, profileDetails.get(Constants.VERIFIED_KARMAYOGI).asBoolean());
                        } else {
                            ((ObjectNode) profileDetails).put(Constants.VERIFIED_KARMAYOGI, Boolean.FALSE);
                        }
                        ((ObjectNode) profileDetails).put(ProfileUtils.Profile.USER_ID, n.get(ProfileUtils.Profile.USER_ID).asText());
                        ((ObjectNode) profileDetails).put(ProfileUtils.Profile.ID, n.get(ProfileUtils.Profile.USER_ID).asText());
                        ((ObjectNode) profileDetails).put(ProfileUtils.Profile.AT_ID, n.get(ProfileUtils.Profile.USER_ID).asText());
                        arrayRes.add(n.get(ProfileUtils.Profile.PROFILE_DETAILS));
                    }
                }

                tagRes.put(sRequest.getField(), arrayRes);
                redisCacheMgr.putCache(Constants.USER_LIST + Constants.UNDER_SCORE + departmentName, arrayRes, networkServerProperties.getRedisUserListReadTimeOut().intValue());

            }
        }
        logger.info("user search result :: {}", new PrettyPrintingMap<>(tagRes));
        return tagRes;
    }

    private int getLimitRequest(int requestSize) {
        Integer limit = networkServerProperties.getDefaultLimit();
        Integer maxLimit = networkServerProperties.getMaxLimit();
        if (requestSize == 0) {
            return limit;
        } else if (requestSize < maxLimit.intValue()) {
            return requestSize;
        }
        return maxLimit.intValue();
    }
}
