
package org.sunbird.cb.hubservices.profile.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cb.hubservices.model.RegistryRequest;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProfileUtils {

	static final RestTemplate restTemplate = new RestTemplate();

	private Logger logger = LoggerFactory.getLogger(ProfileUtils.class);

	@Autowired
	private ConnectionProperties connectionProperties;

	public static List<String> getUserDefaultFields() {
		List<String> userFields = new ArrayList<>();
		userFields.add(Constants.PROFILE_DETAILS_PROFESSIOANAL_DETAILS);
		userFields.add(Constants.PROFILE_DETAILS_EMPLOYMENT_DETAILS);
		userFields.add(Constants.PROFILE_DETAILS_PERSONAL_DETAILS);
		userFields.add(Constants.USER_ID);
		return userFields;
	}

	public enum URL {
		CREATE("/add"), READ("/read"), SEARCH("/search"), UPDATE("/update");

		private String value;

		private URL(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	private static final String UTIL_CLASS = "Utility class";

	public enum STATUS {
		APPROVED, REJECTED, PENDING
	}

	public static class Status {
		private Status() {
			throw new IllegalStateException(UTIL_CLASS);
		}

		public static final String APPROVED = "Approved";
		public static final String REJECTED = "Rejected";
		public static final String PENDING = "Pending";
		public static final String DELETED = "Deleted";

	}

	public static class Profile {
		private Profile() {
			throw new IllegalStateException(UTIL_CLASS);
		}

		public static final String USER_PROFILE = "UserProfile";
		public static final String ID = "id";
		public static final String AT_ID = "@id";
		public static final String USER_ID = "userId";
		public static final String OSID = "osid";
		public static final String FILTERS = "filters";
		public static final String REQUEST = "request";
		public static final String ENTITY_TYPE = "entityType";
		public static final String PROFILE_DETAILS = "profileDetails";

	}

	public static void merge(Map<String, Object> mapLeft, Map<String, Object> mapRight) {
		// go over all the keys of the right map
		for (String key : mapRight.keySet()) {

			Object ml = mapLeft.get(key);
			Object mr = mapRight.get(key);
			// if the left map already has this key, merge the maps that are behind that key
			if (mapLeft.containsKey(key) && ml instanceof HashMap) {
				merge((Map<String, Object>) ml, (Map<String, Object>) mr);

			} else if (mapLeft.containsKey(key) && !(mapLeft.get(key) instanceof HashMap)) {
				mapLeft.put(key, mapRight.get(key));
			} else {
				// otherwise just add the map under that key
				mapLeft.put(key, mapRight.get(key));
			}
		}
	}

	public static void mergeLeaf(Map<String, Object> mapLeft, Map<String, Object> mapRight, String leafKey, String id) {
		// go over all the keys of the right map

		for (String key : mapLeft.keySet()) {

			if (key.equalsIgnoreCase(leafKey) && (mapLeft.get(key) instanceof ArrayList) && !id.isEmpty()) {

				((ArrayList) mapLeft.get(key)).removeIf(
						o -> ((Map) o).get("osid") != null && ((Map) o).get("osid").toString().equalsIgnoreCase(id));
				((ArrayList) mapLeft.get(key)).add(mapRight);

			}
			if (key.equalsIgnoreCase(leafKey) && (mapLeft.get(key) instanceof HashMap)) {
				mapLeft.put(key, mapRight);

			}

		}
	}

	public static ResponseEntity getResponseEntity(String baseUrl, String endPoint, RegistryRequest registryRequest) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add(Constants.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		requestHeaders.add("Authorization",
				"bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJRekw4VVA1dUtqUFdaZVpMd1ZtTFJvNHdqWTg2a2FrcSJ9.TPjV0xLacSbp3FbJ7XeqHoKFN35Rl4YHx3DZNN9pm0o");
		requestHeaders.add("X-Authenticated-User-Token","eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJVMkpUdlpERFY4eG83ZmtfNHd1Yy1kNVJmNjRPTG1oemlRRUhjR25Vc2hNIn0.eyJqdGkiOiJiYTliZGUwNC1iNWUyLTQwYjUtYTcwZi0yNDliZWU1NDM1ODQiLCJleHAiOjE2NTM1OTI1MTQsIm5iZiI6MCwiaWF0IjoxNjUzNTA2MTE0LCJpc3MiOiJodHRwczovL2lnb3QtZGV2LmluL2F1dGgvcmVhbG1zL3N1bmJpcmQiLCJzdWIiOiJmOjkyM2JkYzE4LTUyMGQtNDhkNC1hODRlLTNjZGUxZTY1NWViZDpkYTliNTRjYi03NjM1LTQwMTYtYTU0My1hMWYzZmIyZmFiOGMiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZG1pbi1jbGkiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiIxMDIwYzgyNS00ZmFlLTQ2NzgtYThhYS1lYTkxMWNkMTFlODEiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInNjb3BlIjoiIiwibmFtZSI6Ikp1aGkgQ0JQIEFkbWluIiwicHJlZmVycmVkX3VzZXJuYW1lIjoianVoaWNicGFkbWluX3ZjcGgiLCJnaXZlbl9uYW1lIjoiSnVoaSIsImZhbWlseV9uYW1lIjoiQ0JQIEFkbWluIiwiZW1haWwiOiJqdSoqKioqKipAeW9wbWFpbC5jb20ifQ.O_fYueeKl9RGflVVcpjcFyfVKL6ZOLVqGUAtOJlHXVVaR9YmCxdXtyvVsCn-w1VZ5x-HZtxKk9EXBHlXZYUh9nIOQFjDW1h64IHpoUzcdQINAsurQNM-z7ba5BPxe2i3Rly0W-5oYODm5GYz8mrMv3pmnq5r3IaFxHDqwYvDsQBLCdAxy1r4r8_lKFrKcqPxOLIL92fqOmGbVtsR8XkGO5g1fYS6C5iWAQg8TwVRvbVmLgOKIpl4I4kFG8WiSonEcL8TgCU5bZO7ghj1NwL-bMdf3CfzInP0-m2Kf3jOVU3hs1PHTQGyctv77_cSaQKLlGNJskzesIK0Gt_Y_tw0bw");
		HttpEntity<RegistryRequest> requestEntity = new HttpEntity<>(registryRequest, requestHeaders);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endPoint, HttpMethod.POST, requestEntity,
				Map.class);

		return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
	}

	public List<Map<String, Object>> getUserProfiles(List<String> userIds) {
		StringBuilder builder = new StringBuilder();
		HttpHeaders requestHeaders = new HttpHeaders();
		Map<String, Object> registryRequest = getSearchObject(userIds);
		requestHeaders.add(Constants.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<Object> requestEntity = new HttpEntity<>(registryRequest, requestHeaders);
		builder.append(connectionProperties.getLearnerServiceHost())
				.append(connectionProperties.getUserSearchEndPoint());
		ResponseEntity responseEntity = restTemplate.exchange(builder.toString(), HttpMethod.POST, requestEntity,
				Map.class);
		Map<String, Object> profileResponse = (Map<String, Object>) responseEntity.getBody();
		if (profileResponse != null && "OK".equalsIgnoreCase((String) profileResponse.get("responseCode"))) {
			Map<String, Object> map = (Map<String, Object>) profileResponse.get("result");
			if (map.get(Constants.RESPONSE) != null) {
				List<Map<String, Object>> userProfiles = (List<Map<String, Object>>) ((Map<String, Object>) map
						.get(Constants.RESPONSE)).get("content");
				return userProfiles.stream().filter(userprofile -> userprofile.get(Profile.PROFILE_DETAILS) != null)
						.map(userprofile -> (Map<String, Object>) userprofile.get(Profile.PROFILE_DETAILS))
						.collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}

	public Map<String, Object> readUserProfiles(String userId) {
		StringBuilder builderUrl = new StringBuilder();
		builderUrl.append(connectionProperties.getLearnerServiceHost())
				.append(connectionProperties.getUserReadEndPoint()).append(userId);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity responseEntity = restTemplate.exchange(builderUrl.toString(), HttpMethod.GET, entity, Map.class);

		Map<String, Object> profileResponse = (Map<String, Object>) responseEntity.getBody();
		if (profileResponse != null && "OK".equalsIgnoreCase((String) profileResponse.get("responseCode"))) {
			Map<String, Object> map = (Map<String, Object>) profileResponse.get("result");
			if (map.get(Constants.RESPONSE) != null) {
				return (Map<String, Object>) ((Map<String, Object>) map.get(Constants.RESPONSE))
						.get(Profile.PROFILE_DETAILS);

			}
		}

		return Collections.emptyMap();
	}

	public ResponseEntity updateProfile(String uuid, Map<String, Object> profileObj) {
		StringBuilder builder = new StringBuilder();
		Map<String, Object> requestObject = new HashMap<>();
		Map<String, Object> requestWrapper = new HashMap<>();
		requestWrapper.put(Profile.USER_ID, uuid);
		requestWrapper.put(Profile.PROFILE_DETAILS, profileObj);
		requestObject.put(Profile.REQUEST, requestWrapper);
		RestTemplate restTemplate = new RestTemplate();

		HttpClient httpClient = HttpClientBuilder.create().build();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
		HttpHeaders reqHeaders = new HttpHeaders();
		reqHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Object> requestEntity = new HttpEntity<>(requestObject, reqHeaders);
		builder.append(connectionProperties.getLearnerServiceHost())
				.append(connectionProperties.getUserUpdateEndPoint());
		ResponseEntity responseEntity = restTemplate.exchange(builder.toString(), HttpMethod.PATCH, requestEntity,
				Map.class);
		try {
			ObjectMapper mapper = new ObjectMapper();
			logger.info("profile update response :: {}", mapper.writeValueAsString(responseEntity.getBody()));
		} catch (JsonProcessingException e) {
			logger.error("error:", e);
		}
		return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
	}

	private Map<String, Object> getSearchObject(List<String> userIds) {
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> filters = new HashMap<>();
		filters.put(Profile.USER_ID, userIds);
		request.put(Profile.FILTERS, filters);
		request.put("query", "");
		Map<String, Object> requestWrapper = new HashMap<>();
		requestWrapper.put(Profile.REQUEST, request);
		return requestWrapper;
	}
}
