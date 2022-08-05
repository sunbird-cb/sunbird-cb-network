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
import org.sunbird.cb.hubservices.exception.ApplicationException;
import org.sunbird.cb.hubservices.model.MultiSearch;
import org.sunbird.cb.hubservices.model.Request;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.model.Search;
import org.sunbird.cb.hubservices.profile.handler.ProfileUtils;
import org.sunbird.cb.hubservices.service.IConnectionService;
import org.sunbird.cb.hubservices.service.IProfileService;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;
import org.sunbird.cb.hubservices.util.PrettyPrintingMap;

import java.util.*;

@Service
public class ProfileService implements IProfileService {

	private Logger logger = LoggerFactory.getLogger(ProfileService.class);

	@Autowired
	ConnectionProperties connectionProperties;

	@Autowired
	IConnectionService connectionService;

	@Autowired
	ObjectMapper mapper;

	@Override
	public Response findCommonProfileV2(String userId, int offset, int limit) {
		return connectionService.findSuggestedConnectionsV2(userId, offset, limit);
	}

	@Override
	public Response findProfilesV2(String userId, int offset, int limit) {
		return connectionService.findAllConnectionsIdsByStatusV2(userId, Constants.Status.APPROVED, offset, limit);

	}

	@Override
	public Response findProfileRequestedV2(String userId, int offset, int limit, Constants.DIRECTION direction) {
		return connectionService.findConnectionsRequestedV2(userId, offset, limit, direction);

	}

	@Override
	public Response multiSearchProfiles(String userId, MultiSearch mSearchRequest, String[] sourceFields) {

		Response response = new Response();
		try {
			List<String> connectionIdsToExclude = connectionService.findUserConnectionsV2(userId,
					Constants.Status.APPROVED);
			connectionIdsToExclude.add(userId);
			logger.info("multi search request :: {}", mSearchRequest.toString());

			List<String> tags = new ArrayList<>();
			Map<String, Object> tagRes = new HashMap<>();

			List<String> includeFields = sourceFields != null && !Arrays.asList(sourceFields).isEmpty()
					? Arrays.asList(sourceFields)
					: ProfileUtils.getUserDefaultFields();

			for (Search sRequest : mSearchRequest.getSearch()) {

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
				searchQueryMap.put("offset", mSearchRequest.getOffset());
				searchQueryMap.put("limit", mSearchRequest.getSize());
				searchQueryMap.put("fields", includeFields);

				request.setRequest(searchQueryMap);
				tags.add(sRequest.getField());

				// Hit user search Api
				ResponseEntity<?> responseEntity = ProfileUtils.getResponseEntity(
						connectionProperties.getLearnerServiceHost(), connectionProperties.getUserSearchEndPoint(),
						request);
				JsonNode node = mapper.convertValue(responseEntity.getBody(), JsonNode.class);
				ArrayNode arrayRes = JsonNodeFactory.instance.arrayNode();
				ArrayNode nodes = (ArrayNode) node.get("result").get("response").get("content");
				for (JsonNode n : nodes) {
					if (!connectionIdsToExclude.contains(n.get(ProfileUtils.Profile.USER_ID).asText())) {
						((ObjectNode) n.get(ProfileUtils.Profile.PROFILE_DETAILS)).put(ProfileUtils.Profile.USER_ID,
								n.get(ProfileUtils.Profile.USER_ID).asText());
						((ObjectNode) n.get(ProfileUtils.Profile.PROFILE_DETAILS)).put(ProfileUtils.Profile.ID,
								n.get(ProfileUtils.Profile.USER_ID).asText());
						((ObjectNode) n.get(ProfileUtils.Profile.PROFILE_DETAILS)).put(ProfileUtils.Profile.AT_ID,
								n.get(ProfileUtils.Profile.USER_ID).asText());
						arrayRes.add(n.get(ProfileUtils.Profile.PROFILE_DETAILS));
					}
				}

				tagRes.put(sRequest.getField(), arrayRes);

			}
			logger.info("user search result :: {}", new PrettyPrintingMap<>(tagRes));

			List<Object> finalRes = new ArrayList<>();
			for (Map.Entry entry : tagRes.entrySet()) {
				Map<String, Object> resObjects = new HashMap<>();
				resObjects.put("field", entry.getKey());
				resObjects.put("results", entry.getValue());
				finalRes.add(resObjects);
			}

			response.put(Constants.ResponseStatus.MESSAGE, Constants.ResponseStatus.SUCCESSFUL);
			response.put(Constants.ResponseStatus.DATA, finalRes);
			response.put(Constants.ResponseStatus.STATUS, HttpStatus.OK);

		} catch (Exception e) {
			logger.error(Constants.Message.CONNECTION_EXCEPTION_OCCURED, e);
			throw new ApplicationException(Constants.Message.FAILED_CONNECTION);

		}

		return response;
	}

}
