package org.sunbird.cb.hubservices.serviceimpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cb.hubservices.exception.ApplicationException;
import org.sunbird.cb.hubservices.model.*;
import org.sunbird.cb.hubservices.service.INotificationService;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class NotificationService implements INotificationService {

	private Logger logger = LoggerFactory.getLogger(NotificationService.class);
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	ConnectionProperties connectionProperties;

	@Autowired
	private ProfileService profileService;

	@Override
	public NotificationEvent buildEvent(String eventId, String sender, String reciepient, String status) {

		NotificationEvent notificationEvent = new NotificationEvent();

		if (eventId != null && sender != null && reciepient != null) {

			String fromUUID = sender;

			Map<String, List<String>> recipients = new HashMap<>();
			List<String> toList = Arrays.asList(reciepient);
			recipients.put(connectionProperties.getNotificationTemplateReciepient(), toList);

			logger.info("Notification sender --> {}", fromUUID);
			logger.info("Notification recipients --> {}", recipients);
			// values in body of notification template
			Map<String, Object> tagValues = new HashMap<>();
			tagValues.put(connectionProperties.getNotificationTemplateSender(), getUserName(fromUUID));
			tagValues.put(connectionProperties.getNotificationTemplateTargetUrl(),
					connectionProperties.getNotificationTemplateTargetUrlValue());
			tagValues.put(connectionProperties.getNotificationTemplateStatus(), status);

			notificationEvent.setEventId(eventId);
			notificationEvent.setRecipients(recipients);
			notificationEvent.setTagValues(tagValues);

		}
		return notificationEvent;

	}

	private String getUserName(String uuid) {

		String fromName = null;
		try {
			Response res = null;// profileService.findProfiles(Arrays.asList(uuid),null);
			Map<String, Object> profiles = res.getResult();
			if (profiles.size() > 0) {

				ArrayNode dataNodes = mapper.convertValue(profiles.get(Constants.ResponseStatus.DATA), ArrayNode.class);
				logger.info("dataNodes :-{}", dataNodes);

				JsonNode profilePersonalDetails = dataNodes.get(0).get(Constants.Profile.PERSONAL_DETAILS);
				fromName = profilePersonalDetails.get(Constants.Profile.FIRST_NAME).asText().concat(" ")
						.concat(profilePersonalDetails.get(Constants.Profile.SUR_NAME).asText());

			} else {
				fromName = Constants.Profile.HUB_MEMBER;
			}
		} catch (Exception e) {
			logger.error("Profile name could not be extracted :-{}", e.getMessage());
			fromName = Constants.Profile.HUB_MEMBER;

		}

		return fromName;
	}

	@Override
	public ResponseEntity postEvent(String rootOrg, NotificationEventV2 notificationEventv2) {
		if (rootOrg == null || rootOrg.isEmpty()) {
			throw new ApplicationException(Constants.Message.ROOT_ORG_INVALID);
		}

		ResponseEntity<?> response = null;
		try {
			final String uri = connectionProperties.getNotificationIp()
					.concat(connectionProperties.getNotificationEventEndpoint());
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			logger.info("Notification event v2 value ::", mapper.writeValueAsString(notificationEventv2));

			Map<String, List<NotificationEventV2>> notifications = new HashMap<>();
			notifications.put("notifications", Arrays.asList(notificationEventv2));
			Map<String, Object> nrequest = new HashMap<>();
			nrequest.put("request", notifications);
			logger.info("Notification event v2 request ::", mapper.writeValueAsString(nrequest));

			HttpEntity request = new HttpEntity<>(nrequest, headers);
			response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);

			logger.info(Constants.Message.SENT_NOTIFICATION_SUCCESS, response.getStatusCode());

		} catch (Exception e) {
			logger.error(Constants.Message.SENT_NOTIFICATION_ERROR, e.getMessage());
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return response;

	}

	@Override
	public ResponseEntity postEvent(String rootOrg, NotificationEvent notificationEvent) {
		if (rootOrg == null || rootOrg.isEmpty()) {
			throw new ApplicationException(Constants.Message.ROOT_ORG_INVALID);
		}

		ResponseEntity<?> response = null;
		try {
			final String uri = connectionProperties.getNotificationIp()
					.concat(connectionProperties.getNotificationEventEndpoint());
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");

			HttpEntity request = new HttpEntity<>(notificationEvent, headers);
			response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);

			logger.info(Constants.Message.SENT_NOTIFICATION_SUCCESS, response.getStatusCode());

		} catch (Exception e) {
			logger.error(Constants.Message.SENT_NOTIFICATION_ERROR, e.getMessage());
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return response;

	}

	@Override
	public NotificationEventV2 translate(NotificationEvent notificationEvent) {

		NotificationEventV2 eventV2 = new NotificationEventV2();
		eventV2.setMode(connectionProperties.getNotificationv2Mode());
		eventV2.setDeliveryType(connectionProperties.getNotificationv2DeliveryType());
		List<String> toList = notificationEvent.getRecipients().get(connectionProperties.getNotificationTemplateReciepient());
		eventV2.setIds(toList);

		NotificationConfigV2 configV2 = new NotificationConfigV2();
		configV2.setSender(connectionProperties.getNotificationv2Sender());
		configV2.setSubject(notificationEvent.getEventId());
		eventV2.setConfigV2(configV2);

		NotificationTemplateV2 templateV2 = new NotificationTemplateV2();
		templateV2.setId(connectionProperties.getNotificationv2Id());
		Map<String, String> params = new HashMap<>();
		if (notificationEvent.getEventId().equals(connectionProperties.getNotificationTemplateRequest()))
			params.put("body", replaceWith(connectionProperties.getNotificationv2RequestBody(), notificationEvent.getTagValues()));
		else
			params.put("body", replaceWith(connectionProperties.getNotificationv2ResponseBody(), notificationEvent.getTagValues()));

		templateV2.setParams(params);
		eventV2.setTemplateV2(templateV2);
		return eventV2;
	}


	private String replaceWith(String templateStr, Map<String, Object> tagValues) {
		for (Map.Entry entry : tagValues.entrySet()) {
			if (templateStr.contains(entry.getKey().toString())) {
				templateStr.replace(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		logger.info("replaceWith value ::", templateStr);
		return templateStr;
	}
}
