package org.sunbird.cb.hubservices.profile.handler;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProfileRequestHandler implements IProfileRequestHandler {

	private Logger logger = LoggerFactory.getLogger(ProfileRequestHandler.class);

	@Autowired
	private ProfileUtils profileUtils;

	@Override
	public RegistryRequest createRequest(String uuid, Map<String, Object> request) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

		request.put(ProfileUtils.Profile.USER_ID, uuid);
		request.put(ProfileUtils.Profile.ID, uuid);
		request.put(ProfileUtils.Profile.AT_ID, uuid);

		RegistryRequest registryRequest = new RegistryRequest();
		registryRequest.setId(ProfileUtils.API.CREATE.getValue());
		registryRequest.getRequest().put(ProfileUtils.Profile.USER_PROFILE, request);
		return registryRequest;
	}

	@Override
	public RegistryRequest updateRequest(String uuid, Map<String, Object> request) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

		// search with user id
		ResponseEntity<?> responseEntity = profileUtils.getResponseEntity(ProfileUtils.URL.SEARCH.getValue(),
				searchRequest(uuid));
		Object searchResult ="";
		Object responseBody =responseEntity.getBody();
		if(null!=responseBody) {
			 searchResult = ((Map<String, Object>) ((Map<String, Object>) responseBody).get("result"))
					.get(ProfileUtils.Profile.USER_PROFILE);
		}
		Map<String, Object> search = ((Map<String, Object>) ((List) searchResult).get(0));
		// merge request and search to add osid(s)
		profileUtils.merge(search, request);

		RegistryRequest registryRequest = new RegistryRequest();
		registryRequest.setId(ProfileUtils.API.UPDATE.getValue());
		registryRequest.getRequest().put(ProfileUtils.Profile.USER_PROFILE, search);

		return registryRequest;
	}

	@Override
	public Map<String, Object> updateRequestWithWF(String uuid, List<Map<String, Object>> requests) {

		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> search = new HashMap<>();
		// merge request and search to add osid(s)
		try {
			search = profileUtils.readUserProfiles(uuid);
			logger.info("profile orginal :- {}" , mapper.convertValue(search, JsonNode.class));
			for (Map<String, Object> request : requests) {

				String osid = request.get("osid") == null ? "" : request.get("osid").toString();
				Map<String, Object> toChange = new HashMap<>();
				Object sf = search.get(request.get(Constants.FIELD_KEY));

				if (sf instanceof ArrayList) {
					List<Map<String, Object>> searchFields = (ArrayList) search.get(request.get(Constants.FIELD_KEY));
					for (Map<String, Object> obj : searchFields) {
						if (obj.get("osid") !=null && obj.get("osid").toString().equalsIgnoreCase(osid))
							toChange.putAll(obj);
					}
				}
				if (sf instanceof HashMap) {
					Map<String, Object> searchFields = (Map<String, Object>) search.get(request.get(Constants.FIELD_KEY));
					toChange.putAll(searchFields);

				}

				Map<String, Object> objectMap = (Map<String, Object>) request.get(Constants.FIELD_KEY);
				for (Map.Entry entry : objectMap.entrySet())
					toChange.put((String) entry.getKey(), entry.getValue());

				profileUtils.mergeLeaf(search, toChange, request.get("fieldKey").toString(), osid);
			}
			logger.info("profile merged changes :- {}" , mapper.convertValue(search, JsonNode.class));
		}catch (Exception e){
		    logger.error("Merge profile exception::{}",e);
		}
		return search;
	}

	@Override
	public RegistryRequest searchRequest(String uuid) {
		List types = Arrays.asList(ProfileUtils.Profile.USER_PROFILE);
		Map<String, Map<String, Object>> filters = new HashMap<>();
		filters.put(ProfileUtils.Profile.ID, Stream.of(new AbstractMap.SimpleEntry<>("eq", uuid))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

		RegistryRequest registryRequest = new RegistryRequest();
		registryRequest.setId(ProfileUtils.API.SEARCH.getValue());
		registryRequest.getRequest().put(ProfileUtils.Profile.ENTITY_TYPE, types);
		registryRequest.getRequest().put(ProfileUtils.Profile.FILTERS, filters);
		try {
			ObjectMapper om = new ObjectMapper();
			logger.info(String.format("GET User By ID - request -> %s",om.writeValueAsString(registryRequest)));

		} catch (Exception e) {
			logger.info("Failed to write value as String...");
		}
		return registryRequest;
	}

	@Override
	public RegistryRequest searchRequest(Map params) {
		List types = Arrays.asList(ProfileUtils.Profile.USER_PROFILE);
		logger.info("search params -> {}", params);

		RegistryRequest registryRequest = new RegistryRequest();
		registryRequest.setId(ProfileUtils.API.SEARCH.getValue());
		registryRequest.getRequest().put(ProfileUtils.Profile.ENTITY_TYPE, types);
		if (null != params.get(Constants.OFFSET) && null != params.get("limit")) {
			registryRequest.getRequest().put(Constants.OFFSET, params.get(Constants.OFFSET));
			registryRequest.getRequest().put(Constants.LIMIT, params.get(Constants.LIMIT));
		}

		Map<String, Map<String, Object>> filters = (Map<String, Map<String, Object>>) params
				.get(ProfileUtils.Profile.FILTERS);
		registryRequest.getRequest().put(ProfileUtils.Profile.FILTERS, filters);
		return registryRequest;
	}

	@Override
	public RegistryRequest readRequest(String id) {

		Map<String, Object> params = Stream.of(new AbstractMap.SimpleEntry<>(ProfileUtils.Profile.OSID, id))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		RegistryRequest registryRequest = new RegistryRequest();
		registryRequest.setId(ProfileUtils.API.READ.getValue());
		registryRequest.getRequest().put(ProfileUtils.Profile.USER_PROFILE, params);
		return registryRequest;
	}
}
